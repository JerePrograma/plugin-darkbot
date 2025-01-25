package lol.same.pvptest.pvp;

import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.types.ShipMode;
import eu.darkbot.api.extensions.Configurable;
import eu.darkbot.api.extensions.Feature;
import eu.darkbot.api.extensions.Module;
import eu.darkbot.api.game.enums.PetGear;
import eu.darkbot.api.game.group.GroupMember;
import eu.darkbot.api.game.items.SelectableItem;
import eu.darkbot.api.game.other.Location;
import eu.darkbot.api.game.other.Movable;
import eu.darkbot.api.managers.*;
import eu.darkbot.shared.modules.CollectorModule;
import eu.darkbot.shared.utils.SafetyFinder;
import lol.same.pvptest.PlayerEquipment;
import lol.same.pvptest.pvp.config.PvPConfig;
import lol.same.pvptest.PlayerNames;
import lol.same.pvptest.syncer.SyncerConfig;
import lol.same.pvptest.syncer.SyncerConfigProvider;
import lol.same.pvptest.utils.LogIfChanged;
import lol.same.pvptest.conditionalitems.ConditionsManagement;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

@Feature(name = "PvP", description = "Pelea contra otros jugadores")
public class PvPModule implements Module, Configurable<PvPConfig>, SyncerConfigProvider {
    private PvPConfig config;

    private final HeroAPI hero;
    private final HeroItemsAPI heroItems;
    private final MovementAPI movement;
    private final AttackAPI attack;
    private final SafetyFinder safety;
    private final PlayerEquipment equipment;

    private final ConfigSetting<ShipMode> configOffensive;
    private final ConfigSetting<ShipMode> configRun;
    private final ConfigSetting<ShipMode> configRoam;

    private final CollectorModule collectResourcesModule;

    private long lastAttackTime;

    private boolean isConfigAttackFull = false;
    private boolean isConfigRunFull = false;

    private final FollowLeader followLeader;
    private final TargetSelect targetSelect;

    // Instancia de ConditionsManagement
    private final ConditionsManagement conditionsManagement;

    private enum Status {
        IDLE,
        SAFETY_ESCAPING,
        MAP_TRAVELING,
        APPROACHING_LEADER,
        FIGHTING;
    }

    @NotNull
    private Status status = Status.IDLE;

    public PvPModule(PluginAPI plugin) {
        this.hero = plugin.requireAPI(HeroAPI.class);
        this.heroItems = plugin.requireAPI(HeroItemsAPI.class);
        this.movement = plugin.requireAPI(MovementAPI.class);
        this.attack = plugin.requireAPI(AttackAPI.class);
        this.safety = plugin.requireInstance(SafetyFinder.class);
        this.equipment = plugin.requireInstance(PlayerEquipment.class);

        var configApi = plugin.requireAPI(ConfigAPI.class);
        this.configOffensive = configApi.requireConfig("general.offensive");
        this.configRun = configApi.requireConfig("general.run");
        this.configRoam = configApi.requireConfig("general.roam");

        this.collectResourcesModule = new CollectorModule(plugin);
        this.followLeader = new FollowLeader(plugin);
        this.targetSelect = new TargetSelect(plugin);

        // Inicializar ConditionsManagement
        this.conditionsManagement = new ConditionsManagement(plugin.requireAPI(PluginAPI.class), heroItems);
    }

    @Override
    public String getStatus() {
        String msg = "Error";
        switch (status) {
            case IDLE:
                msg = (config.collectResourcesOnIdle)
                        ? "Recolector: " + collectResourcesModule.getStatus()
                        : "Sin objetivos";
                break;
            case SAFETY_ESCAPING:
                msg = safety.status();
                break;
            case MAP_TRAVELING:
                msg = followLeader.getMapTravelerStatus();
                break;
            case APPROACHING_LEADER:
                msg = "Acercándose al líder";
                break;
            case FIGHTING:
                msg = "Luchando | Time out: " +
                        NANOSECONDS.toSeconds(System.nanoTime() - lastAttackTime)
                        + "/" + config.maxSecondsTimeOut;
                break;
        }
        var leaderInfo = (followLeader.getLeaderId() == 0)
                ? (followLeader.isSelfLeader() ? "Líder: Esta cuenta" : "Sin líder")
                : ("Líder: " + PlayerNames.getName(followLeader.getLeaderId()));
        LogIfChanged.log("Status", msg);
        return msg + " - " + leaderInfo;
    }

    @Override
    public void setConfig(ConfigSetting<PvPConfig> newConfig) {
        this.config = newConfig.getValue();
    }

    @Override
    public boolean canRefresh() {
        return !targetSelect.hasTarget() &&
                collectResourcesModule.canRefresh() &&
                (config.ignoreSafety || safety.tick());
    }

    @Override
    public SyncerConfig getSyncerConfig() {
        var syncerConfig = new SyncerConfig(followLeader.getLeaderId());
        syncerConfig.defaultPetGear = PetGear.GUARD;
        if (config.copyMasterPet)
            syncerConfig.syncedPetGear = EnumSet.of(
                    PetGear.KAMIKAZE,
                    PetGear.BEACON_COMBAT,
                    PetGear.BEACON_HP,
                    PetGear.MEGA_MINE,
                    PetGear.SACRIFICIAL);
        syncerConfig.syncLaser = config.copyLaser;
        syncerConfig.syncRocket = config.copyMasterRocket;
        syncerConfig.syncVants = config.copyVantsFormation;
        syncerConfig.syncEnergyLeech = config.copyEnergyLeech;
        return syncerConfig;
    }

    @Override
    public void onTickModule() {
        updateStatus();
        if (status == Status.SAFETY_ESCAPING) {
            logAndExit("Status");
            return;
        }

        handleTargetSelection();

        handleCombat();

        handleIdleState();
    }

    /**
     * Actualiza el estado actual del módulo.
     */
    private void updateStatus() {
        status = Status.IDLE;
        if (hero.isAttacking()) {
            lastAttackTime = System.nanoTime();
        }
        followLeader.determineLeader();
    }

    /**
     * Maneja la selección y seguimiento de objetivos.
     */
    private void handleTargetSelection() {
        var leader = followLeader.getLeader().orElse(null);

        // Verificar seguridad
        if (!config.ignoreSafety && !safety.tick()) {
            status = Status.SAFETY_ESCAPING;
            return;
        }

        targetSelect.invalidateCurrentIfNeeded(config, lastAttackTime, leader);

        if (!targetSelect.hasTarget() || config.followWhileAttacking) {
            var result = followLeader.follow(config.distanceFromLeader);
            LogIfChanged.log("Resultado de followLeader", result.name());
            if (result == FollowLeaderResult.GOING_TO_MAP)
                status = Status.MAP_TRAVELING;
            else if (result == FollowLeaderResult.GOING_TO_LOCATION)
                status = Status.APPROACHING_LEADER;
        } else {
            LogIfChanged.log("Resultado de followLeader", "No ejecutado");
        }

        if (status == Status.IDLE) {
            var changed = targetSelect.updateBestTarget(config, leader);
            if (changed) {
                System.out.println("Cambio de objetivo");
                lastAttackTime = System.nanoTime();
            }
        }
    }

    /**
     * Maneja la lógica de combate si hay un objetivo presente.
     */
    private void handleCombat() {
        var target = targetSelect.getTarget();

        attack.setTarget(target.orElse(null));
        if (target.isPresent()) {
            if (status == Status.IDLE) {
                status = Status.FIGHTING;
                moveAround(target.get());
            }
            attack.tryLockAndAttack();
            LogIfChanged.log("Tick pvp", "Atacando");
        } else {
            handleNoTarget();
        }
    }

    /**
     * Maneja la lógica cuando no hay un objetivo presente.
     */
    private void handleNoTarget() {
        var leader = followLeader.getLeader().orElse(null);

        if (!hero.isInvisible() &&
                followLeader.getLeaderGroupMember().map(GroupMember::isCloaked).orElse(false)) {
            System.out.println("Camuflándose porque el líder está camuflado");
            heroItems.useItem(SelectableItem.Cpu.CL04K);
        }

        // Implementar uso de ISH-01
        if (config.enableIsh) { // Verificar si ISH está habilitado
            boolean ishUsed = conditionsManagement.useKeyWithConditions(config.ishCondition, SelectableItem.Special.ISH_01);
            if (ishUsed) {
                LogIfChanged.log("Munición", "ISH-01 usado correctamente.");
            } else {
                LogIfChanged.log("Munición", "No se pudo usar ISH-01.");
            }
        }

        autoCloakLogic();
        rechargeShields();

        if (status == Status.IDLE) {
            handleIdleState();
        }
    }

    /**
     * Maneja el estado IDLE del módulo.
     */
    private void handleIdleState() {
        if ((config.rechargeShields && isConfigAttackFull && isConfigRunFull)
                || (!config.rechargeShields && config.changeConfig)) {
            hero.setMode(ShipMode.of(configRoam.getValue().getConfiguration(), hero.getFormation()));
        }

        if (config.collectResourcesOnIdle && collectResourcesModule.isNotWaiting()) {
            collectResourcesModule.findBox();
            var leader = followLeader.getLeader().orElse(null);
            if (leader == null || (collectResourcesModule.currentBox != null &&
                    collectResourcesModule.currentBox.distanceTo(leader) < config.distanceFromLeader)) {
                LogIfChanged.log("Tick pvp", "Recolectando caja " + collectResourcesModule.currentBox);
                collectResourcesModule.onTickModule();
            } else {
                LogIfChanged.log("Tick pvp", "Nada que recolectar");
            }
        } else if (!config.collectResourcesOnIdle && leaderIsNullOrOutOfMap()) {
            LogIfChanged.log("Tick pvp", "Movimiento aleatorio");
            movement.moveRandom();
        } else {
            LogIfChanged.log("Tick pvp", "Detenido");
        }

        var leader = followLeader.getLeader().orElse(null);
        if (leader != null &&
                movement.getDestination().distanceTo(leader) > config.distanceFromLeader) {
            System.out.println("Cancelando movimiento lejos del líder");
            movement.stop(true);
        }
    }

    /**
     * Verifica si el líder está ausente o fuera del mapa.
     */
    private boolean leaderIsNullOrOutOfMap() {
        return followLeader.getLeader().orElse(null) == null &&
                (!movement.isMoving() || movement.isOutOfMap());
    }

    /**
     * Registra un mensaje de log y retorna desde el método.
     *
     * @param logCategory Categoría del log.
     */
    private void logAndExit(String logCategory) {
        LogIfChanged.log(logCategory, getStatus());
    }

    /**
     * Implementa la lógica de movimiento alrededor del objetivo.
     *
     * @param target El objetivo alrededor del cual moverse.
     */
    private void moveAround(Movable target) {
        double distance = hero.getLocationInfo().distanceTo(target);
        var targetLoc = target.getLocationInfo().destinationInTime(400);
        if (distance > 600) {
            if (movement.canMove(targetLoc)) {
                movement.moveTo(targetLoc);
                if (target.getSpeed() > hero.getSpeed())
                    hero.setMode(ShipMode.of(configRun.getValue().getConfiguration(), hero.getFormation()));
            }
        } else {
            hero.setMode(ShipMode.of(configOffensive.getValue().getConfiguration(), hero.getFormation()));
            movement.moveTo(Location.of(targetLoc, (int) (Math.random() * 360), distance));
        }
    }

    /**
     * Lógica de cloaking automático.
     */
    private void autoCloakLogic() {
        if (config.autoCloak.autoCloakShip && !hero.isInvisible() &&
                System.nanoTime() - lastAttackTime > SECONDS.toNanos(config.autoCloak.secondsOfWaiting)) {
            if (config.autoCloak.onlyPvpMaps && !hero.getMap().isPvp())
                return;
            var result = heroItems.useItem(SelectableItem.Cpu.CL04K);
            System.out.println("Cloak aplicado automáticamente, resultado: " + result);
        }
    }

    /**
     * Lógica para recargar escudos.
     */
    private void rechargeShields() {
        if (config.rechargeShields) {
            if (!isConfigAttackFull) {
                hero.setMode(ShipMode.of(configOffensive.getValue().getConfiguration(), hero.getFormation()));
                if ((hero.getHealth().getMaxShield() > 10000
                        && hero.getHealth().shieldPercent() > 0.9)
                        || hero.getHealth().getShield() >= hero.getHealth().getMaxShield()) {
                    isConfigAttackFull = true;
                }
            } else if (!isConfigRunFull) {
                hero.setMode(ShipMode.of(configRun.getValue().getConfiguration(), hero.getFormation()));
                if ((hero.getHealth().getMaxShield() > 10000
                        && hero.getHealth().shieldPercent() > 0.9)
                        || hero.getHealth().getShield() >= hero.getHealth().getMaxShield()) {
                    isConfigRunFull = true;
                }
            }
        }
    }
}
