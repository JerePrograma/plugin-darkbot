package lol.same.pvptest.pvp.config;

import eu.darkbot.api.config.annotations.Configuration;
import eu.darkbot.api.config.annotations.Dropdown;
import eu.darkbot.api.config.annotations.Option;
import eu.darkbot.api.config.annotations.Number;
import eu.darkbot.api.config.types.Condition;
import eu.darkbot.api.game.enums.EntityEffect;
import eu.darkbot.api.game.items.SelectableItem;
import lol.same.pvptest.conditionalitems.PercentHealthLessThanCondition;

import java.util.HashSet;
import java.util.Set;

@Configuration("pvp_module")
public class PvPConfig {
    // Opciones para Municiones Adicionales (Módulo 0)
    @Option("general.enable_ucb100")
    public boolean enableUCB100 = true;

    @Option("general.enable_rsb75")
    public boolean enableRSB75 = true;

    @Option("general.enable_rcb140")
    public boolean enableRCB140 = true;

    @Option("general.enable_abl")
    public boolean enableABL = true;

    @Option("general.enable_pib")
    public boolean enablePIB = true;

    @Option("general.enable_rci3")
    public boolean enableRCI3 = true;

    @Option("general.enable_pld8")
    public boolean enablePLD8 = true;

    @Option("general.enable_npc_weapons")
    public boolean enableNpcWeapons = true;

    // Opción para habilitar ISH
    @Option("general.enable_ish")
    public boolean enableIsh = false;

    // Condición para usar ISH-01
    @Option("general.ish_condition")
    public Condition ishCondition = new PercentHealthLessThanCondition(50); // Usar ISH si la salud < 50%

    // Opción para habilitar PEM
    @Option("general.enable_pem")
    public boolean enablePem = false;

    // Configuración para PEM-01
    @Option("general.pem")
    public SelectableItem.Special pemItem = SelectableItem.Special.EMP_01; // Usar el enum correcto

    @Option("general.pem.desc")
    public String pemDescription = "Condiciones PEM-01";

    // Tiempo de espera entre usos (en segundos)
    @Option("general.item_cooldown_seconds")
    @Number(min = 0, max = 60, step = 1)
    public int itemCooldownSeconds = 4; // 4 segundos de espera

    // Configuración de Auto Cloak
    @Option("auto_cloak")
    public AutoCloak autoCloak = new AutoCloak();

    // Configuración del Efecto de Infección utilizando EntityEffect
    @Option("general.infection_effect")
    public EntityEffect infectionEffect = EntityEffect.INFECTION; // Usar el enum existente

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

    // Configuración de timeout
    @Option("defense.max_time_out")
    public long maxTimeOut = 60000; // en milisegundos, por ejemplo
    // Opción para habilitar ISH

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

    // Configuración para ISH-01
    @Option("general.ish")
    public String ishItem = "ISH-01";

    @Option("general.ish.desc")
    public String ishDescription = "Condiciones ISH-01";

    // Condición para usar PEM-01
    @Option("general.pem_condition")
    public Condition pemCondition = new PercentHealthLessThanCondition(50); // Usar PEM si la salud < 50%

}
