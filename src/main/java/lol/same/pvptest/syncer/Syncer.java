package lol.same.pvptest.syncer;

import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.annotations.Configuration;
import eu.darkbot.api.config.annotations.Option;
import eu.darkbot.api.config.annotations.Number;
import eu.darkbot.api.extensions.Behavior;
import eu.darkbot.api.extensions.Configurable;
import eu.darkbot.api.extensions.Feature;
import eu.darkbot.api.extensions.Installable;
import eu.darkbot.api.game.entities.Player;
import eu.darkbot.api.game.enums.EntityEffect;
import eu.darkbot.api.game.items.ItemUseResult;
import eu.darkbot.api.game.items.SelectableItem;
import eu.darkbot.api.managers.*;
import eu.darkbot.api.utils.ItemNotEquippedException;
import lol.same.pvptest.PlayerEquipment;
import lol.same.pvptest.PlayerMapTracker;
import lol.same.pvptest.PlayerNames;
import lol.same.pvptest.pvp.LeaderPriority;
import lol.same.pvptest.syncer.messages.*;
import lol.same.pvptest.utils.LogIfChanged;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Feature(name = "Syncer", description = "Sincroniza las acciones de varios bots", enabledByDefault = true)
public class Syncer implements Behavior, Installable, Configurable<Syncer.Config> {
    public static final Set<Integer> talkedTo = new HashSet<>();

    private final HeroAPI hero;
    private final PetAPI pet;
    private final HeroItemsAPI items;
    private final BotAPI bot;
    private final RepairAPI repair;
    private final Collection<? extends Player> players;
    private final PlayerEquipment equipment;

    private int userId;
    private SyncerThread thread = null;
    private int lastMapId;
    private PetChanged lastPetChanged;
    private LaserSelected lastLaserSelected;
    private RocketSelected lastRocketSelected;
    private VantsChanged lastVantsChanged;
    private MyLeaderPriority lastMyLeaderPriority;
    private long lastSentEverything;
    private Syncer.Config config;
    private boolean died;
    private boolean energyLeechUsed;

    @Configuration("syncer_config")
    public static class Config {
        @Option("syncer.can_be_leader")
        public boolean canBeLeader;
    
        @Option("syncer.leader_priority")
        @Number(step = 1)
        public int leaderPriority;    
    }

    @Override
    public void setConfig(ConfigSetting<Config> newConfig) {
        this.config = newConfig.getValue();
    }

    public Syncer(PluginAPI plugin) {
        this.hero = plugin.requireAPI(HeroAPI.class);
        this.pet = plugin.requireAPI(PetAPI.class);
        this.items = plugin.requireAPI(HeroItemsAPI.class);
        this.bot = plugin.requireAPI(BotAPI.class);
        this.repair = plugin.requireAPI(RepairAPI.class);
        this.players = plugin.requireAPI(EntitiesAPI.class).getPlayers();
        this.equipment = plugin.requireInstance(PlayerEquipment.class);
    }

    @Override
    public void onTickBehavior() {
        if (thread == null)
            return;
        sendOwnChanges();
        var config = (SyncerConfig) null;
        if (bot.getModule() instanceof SyncerConfigProvider)
            config = ((SyncerConfigProvider) bot.getModule()).getSyncerConfig();
        this.readOthersChanges(config);
    }

    @Override
    public void onStoppedBehavior() {
        if (thread == null)
            return;
        sendOwnChanges();
    }

    @Override
    public void install(PluginAPI pluginAPI) {
        if (thread == null) {
            thread = new SyncerThread();
            Executors.newSingleThreadExecutor().submit(thread);
        }
    }

    @Override
    public void uninstall() {
        if (thread != null) {
            thread.stop();
            thread = null;
        }
    }

    private void sendOwnChanges() {
        LogIfChanged.log("User ID", "" + hero.getId());
        LogIfChanged.log("Is valid", "" + hero.isValid());

        if (userId == 0) {
            if (hero.getId() == 0)
                return;
            userId = hero.getId();
            LeaderPriority.setMyId(userId);
        }

        LogIfChanged.log("Pet", pet.isEnabled() + ", " + pet.getGear());

        if (repair.isDestroyed() && !died) {
            died = true;
            System.out.println("Deshabilitando ser líder");
        } else if (died && !repair.isDestroyed()) {
            var currentLeader = LeaderPriority.getHighestPriorityId();
            if (currentLeader.isPresent()) {
                var leader = players.stream()
                        .filter(p -> p.getId() == currentLeader.get())
                        .findAny();
                if (leader.isPresent() && leader.get().distanceTo(hero) < 1_500) {
                    died = false;
                    System.out.println("Puede volver a ser líder");
                }
            }
        }

        if (System.nanoTime() - lastSentEverything > SECONDS.toNanos(10)) {
            thread.sendMessage(new PlayerName(userId, hero.getEntityInfo().getUsername()));
            lastPetChanged = null;
            lastLaserSelected = null;
            lastRocketSelected = null;
            lastVantsChanged = null;
            lastMapId = 0;
            lastMyLeaderPriority = null;
            lastSentEverything = System.nanoTime();
        }

        var priorityMsg = new MyLeaderPriority(userId, config.canBeLeader && !died, config.leaderPriority);
        if (!priorityMsg.equals(lastMyLeaderPriority)) {
            LogIfChanged.log("Enviando prioridad", priorityMsg.debug());
            lastMyLeaderPriority = priorityMsg;
            LeaderPriority.setMyPriority(config.canBeLeader && !died, config.leaderPriority);
            thread.sendMessage(priorityMsg);
        }

        if (!hero.isValid())
            return;

        var map = hero.getMap();
        if (map != null && map.getId() != lastMapId) {
            var location = hero.getLocationInfo();
            if (location != null) {
                thread.sendMessage(new MyLocation(userId, location, map));
                lastMapId = map.getId();
            }
        }

        var petChanged = new PetChanged(userId, pet.isEnabled(), pet.getGear());
        if (!petChanged.equals(lastPetChanged)) {
            lastPetChanged = petChanged;
            thread.sendMessage(lastPetChanged);
        }

        var laser = hero.getLaser();
        if (laser != null) {
            var laserSelected = new LaserSelected(userId, laser);
            if (!laserSelected.equals(lastLaserSelected)) {
                lastLaserSelected = laserSelected;
                thread.sendMessage(laserSelected);
            }
        }

        var rocket = hero.getRocket();
        if (rocket != null) {
            var rocketSelected = new RocketSelected(userId, rocket);
            if (!rocketSelected.equals(lastRocketSelected)) {
                lastRocketSelected = rocketSelected;
                thread.sendMessage(rocketSelected);
            }
        }

        var formation = hero.getFormation();
        if (formation != null) {
            var vantsChanged = new VantsChanged(userId, formation);
            if (!vantsChanged.equals(lastVantsChanged)) {
                lastVantsChanged = vantsChanged;
                thread.sendMessage(vantsChanged);
            }
        }

        if (!energyLeechUsed && hero.hasEffect(EntityEffect.ENERGY_LEECH))
            thread.sendMessage(new EnergyLeechUsed(userId));
        energyLeechUsed = hero.hasEffect(EntityEffect.ENERGY_LEECH);
    }

    private void readOthersChanges(@Nullable SyncerConfig config) {
        while (true) {
            var messageOpt = thread.poll();
            if (messageOpt.isEmpty())
                break;
            var message = messageOpt.get();

            LogIfChanged.log("Mensaje " + message.getClass().getSimpleName() + " de " + message.fromPlayerId,
                    message.debug());

            if (message.fromPlayerId == 0)
                return;

            if (message instanceof PlayerName) {
                PlayerNames.setName(message.fromPlayerId, ((PlayerName) message).getName());
                continue;
            }

            if (message.fromPlayerId == userId)
                continue;

            talkedTo.add(message.fromPlayerId);

            if (message instanceof MyLocation) {
                var msg = (MyLocation) message;
                PlayerMapTracker.updateLastPlayerLocation(msg.fromPlayerId, msg.getMapId(), msg.getLocation());
                continue;
            } else if (message instanceof MyLeaderPriority) {
                var priority = ((MyLeaderPriority) message).getPriority();
                LeaderPriority.setPriority(message.fromPlayerId, priority.isPresent(), priority.orElse(0));
                continue;
            } else if (message instanceof Disconnected) {
                LeaderPriority.setPriority(message.fromPlayerId, false, 0);
                continue;
            }

            if (config == null || config.leaderId == 0 || message.fromPlayerId != config.leaderId ||
                    hero.getId() == 0 || !hero.isValid())
                continue;

            if (message instanceof PetChanged) {
                var msg = (PetChanged) message;
                var syncedGear = msg.getGear().filter(g -> config.syncedPetGear.contains(g));
                var gear = syncedGear.orElse(config.defaultPetGear);
                if (syncedGear.isPresent()) {
                    pet.setEnabled(msg.isEnabled());
                } else if (gear != null) {
                    pet.setEnabled(true);
                }
                if (gear != null) {
                    try {
                        pet.setGear(gear);
                    } catch (ItemNotEquippedException e) {
                        System.out.println(gear.getName() + " no equipado");
                    }
                }
            } else if (message instanceof LaserSelected && config.syncLaser) {
                var laser = ((LaserSelected) message).getLaser();
                var result = items.useItem(laser);
                if (result != ItemUseResult.ALREADY_SELECTED)
                    System.out.println("Laser cambiado a " + laser.getId() + ": " + result);
            } else if (message instanceof RocketSelected && config.syncRocket) {
                var rocket = ((RocketSelected) message).getRocket();
                var idNormalized = rocket.getId().toLowerCase()
                        .replace("-", "")
                        .replace("_", "");
                if (!idNormalized.contains("dcr250") &&
                        !idNormalized.contains("ric3") &&
                        !idNormalized.contains("pld8")) {
                    var result = items.useItem(rocket);
                    if (result != ItemUseResult.ALREADY_SELECTED)
                        System.out.println("Misil cambiado a " + rocket.getId() + ": " + result);
                }
            } else if (message instanceof VantsChanged && config.syncVants) {
                var msg = (VantsChanged) message;
                var result = items.useItem(msg.getFormation());
                if (result != ItemUseResult.ALREADY_SELECTED)
                    System.out.println("Vants cambiados a " + msg.getFormation().getId() + ": " + result);
            } else if (message instanceof EnergyLeechUsed && config.syncEnergyLeech) {
                var result = items.useItem(SelectableItem.Special.Tech.ENERGY_LEECH);
                System.out.println("Usar energy leech: " + result);
            }
        }
    }
}
