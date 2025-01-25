package lol.same.pvptest.pvp.config;

import eu.darkbot.api.config.annotations.Configuration;
import eu.darkbot.api.config.annotations.Dropdown;
import eu.darkbot.api.config.annotations.Option;
import eu.darkbot.api.config.annotations.Number;
import eu.darkbot.api.config.types.Condition;
import eu.darkbot.api.game.enums.EntityEffect;
import lol.same.pvptest.conditionalitems.PercentHealthLessThanCondition;

import java.util.HashSet;
import java.util.Set;

@Configuration("pvp_module")
public class PvPConfig {

    @Option("pvp_module.ammo_config")
    public AmmoConfig ammoConfig = new AmmoConfig();

    // Configuración de Auto Cloak
    @Option("auto_cloak")
    public AutoCloak autoCloak = new AutoCloak();

    // Configuración del Efecto de Infección utilizando EntityEffect
    @Option("general.infection_effect")
    public EntityEffect infectionEffect = EntityEffect.INFECTION;

    // Tiempo de espera entre usos (en segundos)
    @Option("general.item_cooldown_seconds")
    @Number(min = 0, max = 60, step = 1)
    public int itemCooldownSeconds = 4; // 4 segundos de espera

    // Otras opciones relacionadas con auto camuflaje
    @Option("general.auto_cloak")
    public boolean enableAutoCloak = false;

    // Opción para activar el recolector en idle
    @Option("pvp_module.enable_collector")
    public boolean enableCollector = false;

    // Opción para cambiar la configuración automáticamente
    @Option("pvp_module.change_config")
    public boolean changeConfig = false;

    // Opción para usar la configuración de huida
    @Option("pvp_module.run_config")
    public boolean runConfig = false;

    // Distancia desde el líder
    @Option("pvp.distance_from_leader")
    public double distanceFromLeader = 500.0;

    // Configuración de timeout en milisegundos
    @Option("defense.max_time_out_ms")
    public long maxTimeOut = 60000; // en milisegundos

    // Configuración de timeout en segundos
    @Option("defense.max_time_out_sec")
    @Number(min = 0, max = 180, step = 1)
    public int maxSecondsTimeOut = 10;

    // Otras opciones relacionadas con seguimiento y copiado
    @Option("pvp.follow_while_attacking")
    public boolean followWhileAttacking = true;

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

    @Option("pvp_module.max_range_enemy_attacked")
    @Number(min = 200, max = 4000, step = 100)
    public int rangeForAttackedEnemy = 1000;

    @Option("pvp.attack_disruptor")
    public boolean attackDisruptor = true;

    @Option("pvp.avoid_disruptor_redirect")
    public boolean avoidDisruptorRedirect = true;

    @Option("pvp.avoid_citadel_draw_fire")
    public boolean avoidCitadelDrawFire = true;

    // Configuración para ISH-01
    @Option("general.ish")
    public String ishItem = "ISH-01";

    @Option("general.ish.desc")
    public String ishDescription = "Condiciones ISH-01";
}
