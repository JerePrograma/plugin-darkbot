package lol.same.pvptest.pvp.config;

import eu.darkbot.api.config.annotations.Configuration;
import eu.darkbot.api.config.annotations.Dropdown;
import eu.darkbot.api.config.annotations.Option;
import eu.darkbot.api.config.annotations.Number;

import java.util.HashSet;
import java.util.Set;

@Configuration("pvp_module")
public class PvPConfig {
    @Option("pvp.follow_while_attacking")
    public boolean followWhileAttacking = true;

    @Option("pvp.distance_from_leader")
    @Number(min = 0, step = 100, max = 100000)
    public int distanceFromLeader = 0;

    @Option("pvp.copy_laser")
    public boolean copyLaser = true;

    @Option("sentinel.copy_master_rocket")
    public boolean copyMasterRocket = true;

    @Option("sentinel.copy_master_pet")
    public boolean copyMasterPet = true;

    @Option("pvp.copy_vants_formation")
    public boolean copyVantsFormation = true;

    @Option("pvp.copy_energy_leech")
    public boolean copyEnergyLeech = false;

    @Option("pvp.target_select_modes")
    @Dropdown(options = TargetSelectModeOptions.class, multi = true)
    public Set<TargetSelectMode> targetSelectModes = new HashSet<>();

    @Option("sentinel.ignore_safety")
    public boolean ignoreSafety = false;

    @Option("pvp_module.recharge_shields")
    public boolean rechargeShields = true;

    @Option("pvp_module.enable_collector")
    public boolean collectResourcesOnIdle = false;

    @Option("pvp_module.change_config")
    public boolean changeConfig = true;

    @Option("defense.max_time_out")
    @Number(min = 0, max = 180, step = 1)
    public int maxSecondsTimeOut = 10;

    @Option("pvp_module.max_range_enemy_attacked")
    @Number(min = 200, max = 4000, step = 100)
    public int rangeForAttackedEnemy = 1000;

    @Option("pvp.attack_disruptor")
    public boolean attackDisruptor = true;

    @Option("pvp.avoid_disruptor_redirect")
    public boolean avoidDisruptorRedirect = true;

    @Option("pvp.avoid_citadel_draw_fire")
    public boolean avoidCitadelDrawFire = true;

    @Option("general.auto_cloak")
    public AutoCloak autoCloak = new AutoCloak();

    @Option("anti_push")
    public AntiPush antiPush = new AntiPush();
}
