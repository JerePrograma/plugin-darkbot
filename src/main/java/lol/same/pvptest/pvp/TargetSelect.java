package lol.same.pvptest.pvp;

import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.game.entities.Npc;
import eu.darkbot.api.game.entities.Player;
import eu.darkbot.api.game.entities.Ship;
import eu.darkbot.api.managers.EntitiesAPI;
import eu.darkbot.api.managers.GroupAPI;
import eu.darkbot.api.managers.HeroAPI;
import lol.same.pvptest.pvp.config.PvPConfig;
import lol.same.pvptest.pvp.config.TargetSelectMode;
import lol.same.pvptest.pvp.config.TargetSelectModeOptions;
import lol.same.pvptest.syncer.Syncer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static java.util.concurrent.TimeUnit.SECONDS;

public class TargetSelect {
    private final HeroAPI hero;
    private final GroupAPI group;
    private final Collection<? extends Player> players;
    private final Collection<? extends Npc> npcs;

    private @Nullable Ship currentTarget;
    private @Nullable TargetSelectMode currentTargetReason;
    private final List<Integer> playersKilled = new ArrayList<>();

    public TargetSelect(PluginAPI plugin) {
        this.hero = plugin.requireAPI(HeroAPI.class);
        this.group = plugin.requireAPI(GroupAPI.class);

        var entities = plugin.requireAPI(EntitiesAPI.class);
        this.players = entities.getPlayers();
        this.npcs = entities.getNpcs();
    }

    public void invalidateCurrentIfNeeded(PvPConfig config, long lastAttackTime, @Nullable Player leader) {
        if (currentTarget != null) {
            if (!currentTarget.isValid()) {
                if (currentTarget.getHealth().getHp() < 30_000) {
                    System.out.println("Enemigo destruido: " + currentTarget.getEntityInfo().getUsername());
                    playersKilled.add(currentTarget.getId());
                } else {
                    System.out.println("El enemigo desapareció");
                }
                currentTarget = null;
            } else if (group.hasGroup() && group.getMembers().stream().anyMatch(m -> m.getId() == currentTarget.getId())) {
                System.out.println("Dejando de atacar porque se unió al grupo");
                currentTarget = null;
            } else if (currentTarget.distanceTo(hero) > config.rangeForAttackedEnemy) {
                System.out.println("El enemigo escapó");
                currentTarget = null;
            } else if (System.nanoTime() - lastAttackTime > SECONDS.toNanos(config.maxSecondsTimeOut)) {
                System.out.println("Timeout");
                currentTarget = null;
            } else if (config.avoidDisruptorRedirect && currentTarget.hasEffect(320)) {
                System.out.println("Deteniendo ataque porque el enemigo uso redirect");
                currentTarget = null;
            } else if (config.avoidCitadelDrawFire && players.stream()
                    .anyMatch(p -> p.hasEffect(36) && p.distanceTo(hero) < 1_500 &&
                            !p.getEntityInfo().isEnemy())) {
                System.out.println("Deteniendo ataque porque el enemigo uso atracción de fuego");
                currentTarget = null;
            } else if (!config.targetSelectModes.contains(currentTargetReason)) {
                System.out.println("Dejando de atacar porque se desactivo el modo con el que se eligió al objetivo");
                currentTarget = null;
            } else if (leader != null && leader.getTarget() == null && (
                    currentTargetReason == TargetSelectMode.HELP_ATTACK_PLAYERS ||
                    currentTargetReason == TargetSelectMode.HELP_ATTACK_NPCS)) {
                System.out.println("Dejando de atacar porque el líder dejo de atacar");
                currentTarget = null;
            }
        }
        if (currentTarget == null)
            currentTargetReason = null;
    }

    /// Devuelve true si se seleccionó un objetivo distinto al anterior.
    public boolean updateBestTarget(PvPConfig config, @Nullable Player leader) {
        if (config.avoidCitadelDrawFire && players.stream()
                .anyMatch(p -> p.hasEffect(36) && p.distanceTo(hero) < 1_500 &&
                        !p.getEntityInfo().isEnemy()))
            return false;
        var modes = config.targetSelectModes;
        var prevTarget = currentTarget;
        if (leader != null) {
            players.stream()
                    .filter(p -> p.getEntityInfo().isEnemy() &&
                            p.isAttacking(leader) &&
                            (config.attackDisruptor || !p.getShipType().toLowerCase().contains("disruptor")) &&
                            (!config.avoidDisruptorRedirect || !p.hasEffect(320)))
                    .min(Comparator.comparingDouble(s -> s.getLocationInfo().distanceTo(hero)))
                    .ifPresent(p -> trySetTargetWithReason(p, TargetSelectMode.DEFEND_LEADER_FROM_PLAYERS, modes));
            if (!hasTarget() ||
                    currentTargetReason == TargetSelectMode.HELP_ATTACK_PLAYERS ||
                    currentTargetReason == TargetSelectMode.HELP_ATTACK_NPCS) {
                var leaderTarget = leader.getTarget();
                if (leaderTarget instanceof Player && ((Player) leaderTarget).getEntityInfo().isEnemy())
                    trySetTargetWithReason((Ship) leaderTarget, TargetSelectMode.HELP_ATTACK_PLAYERS, modes);
                else if (leaderTarget instanceof Npc)
                    trySetTargetWithReason((Ship) leaderTarget, TargetSelectMode.HELP_ATTACK_NPCS, modes);
                else if (leaderTarget != null)
                    System.out.println("target no es una instancia conocida: " + leaderTarget.getClass().getName());
            }
        }
        if (!hasTarget())
            players.stream()
                .filter(p -> p.getEntityInfo().isEnemy() &&
                        p.isAttacking() &&
                        p.getTarget() != null &&
                        (Syncer.talkedTo.contains(p.getTarget().getId()) || (
                            group.hasGroup() && group.getMembers().stream().anyMatch(m -> m.getId() == p.getTarget().getId()))) &&
                        (config.attackDisruptor || !p.getShipType().toLowerCase().contains("disruptor")) &&
                        (!config.avoidDisruptorRedirect || !p.hasEffect(320)))
                .min(Comparator.comparingDouble(s -> s.getLocationInfo().distanceTo(hero)))
                .ifPresent(p -> trySetTargetWithReason(p, TargetSelectMode.DEFEND_GROUP_FROM_PLAYERS, modes));
        if (currentTarget != null)
            return prevTarget == null || prevTarget.getId() != currentTarget.getId();
        players.stream()
            .filter(p -> p.getEntityInfo().isEnemy() &&
                    p.isAttacking(hero) &&
                    (config.attackDisruptor || !p.getShipType().toLowerCase().contains("disruptor")) &&
                    (!config.avoidDisruptorRedirect || !p.hasEffect(320)) &&
                    (!config.avoidCitadelDrawFire || !p.hasEffect(36)))
            .min(Comparator.comparingDouble(s -> s.getLocationInfo().distanceTo(hero)))
            .ifPresent(p -> trySetTargetWithReason(p, TargetSelectMode.DEFEND_SELF_FROM_PLAYERS, modes));
        if (hasTarget())
            return true;
        npcs.stream()
            .filter(n -> n.getInfo().getShouldKill() && n.isAttacking(hero))
            .min(Comparator.comparingDouble(s -> s.getLocationInfo().distanceTo(hero)))
            .ifPresent(n -> trySetTargetWithReason(n, TargetSelectMode.DEFEND_SELF_FROM_NPCS, modes));
        if (hasTarget())
            return true;
        npcs.stream()
                .filter(n -> n.getInfo().getShouldKill())
                .min(Comparator.comparingDouble(s -> s.getLocationInfo().distanceTo(hero)))
                .ifPresent(n -> trySetTargetWithReason(n, TargetSelectMode.ATTACK_NPCS, modes));
        return hasTarget();
    }

    public Optional<Ship> getTarget() {
        return Optional.ofNullable(currentTarget);
    }

    public boolean hasTarget() {
        return currentTarget != null;
    }

    private void trySetTargetWithReason(@NotNull Ship newTarget, TargetSelectMode reason, Set<TargetSelectMode> enabledModes) {
        if (!enabledModes.contains(reason))
            return;
        if (currentTarget == null || currentTarget.getId() != newTarget.getId())
            System.out.println("Nuevo objetivo: " + newTarget.getEntityInfo().getUsername() +
                    ", razón: " + new TargetSelectModeOptions().getText(reason));
        currentTarget = newTarget;
        currentTargetReason = reason;
    }

    private ArrayList<Integer> getIgnoredPlayers(boolean antiPush, int maxKills) {
        ArrayList<Integer> playersToIgnore = new ArrayList<>();

        if (antiPush) {
            playersKilled.forEach(id -> {
                if (!playersToIgnore.contains(id)
                        && Collections.frequency(playersKilled, id) >= maxKills) {
                    playersToIgnore.add(id);
                }
            });
        }

        return playersToIgnore;
    }
}
