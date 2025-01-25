package lol.same.pvptest.pvp.config;

import eu.darkbot.api.config.annotations.Configuration;
import eu.darkbot.api.config.annotations.Option;
import eu.darkbot.api.config.annotations.Number;
import eu.darkbot.api.config.types.Condition;
import eu.darkbot.api.game.items.SelectableItem;
import lol.same.pvptest.conditionalitems.PercentHealthLessThanCondition;

@Configuration("ammo_module")
public class AmmoConfig {
    @Option("ammo_module.ucb")
    public Boolean ucb = false;

    @Option("ammo_module.rsb")
    public Boolean rsb = false;

    @Option("ammo_module.rcb")
    public Boolean rcb = false;

    @Option("ammo_module.abl")
    public Boolean abl = false;

    @Option("ammo_module.pib")
    public Boolean pib = false;

    @Option("ammo_module.use_pld8")
    public boolean usePLD8 = true;

    @Option("ammo_module.use_ric3")
    public boolean useRIC3 = true;

    @Option("ammo_module.pld8_cooldown")
    @Number(min = 0, step = 1, max = 60)
    public int pld8Cooldown = 4;

    @Option("ammo_module.ric3_cooldown")
    @Number(min = 0, step = 1, max = 60)
    public int ric3Cooldown = 10;

    // Opciones Añadidas para PEM
    @Option("ammo_module.enable_pem")
    public boolean enablePem = false;

    @Option("ammo_module.pem_item")
    public SelectableItem.Special pemItem = SelectableItem.Special.EMP_01;

    @Option("ammo_module.pem_condition")
    public Condition pemCondition = new PercentHealthLessThanCondition(50); // Usar PEM si la salud < 50%

    @Option("ammo_module.pem_cooldown")
    @Number(min=0, step=1, max=60)
    public int pemCooldown = 10;

    // Opciones Añadidas para ISH
    @Option("ammo_module.enable_ish")
    public boolean enableIsh = false;

    // Condición para usar ISH-01
    @Option("ammo_module.ish_condition")
    public Condition ishCondition = new PercentHealthLessThanCondition(50); // Usar ISH si la salud < 50%

    @Option("ammo_module.ish_cooldown")
    @Number(min=0, step=1, max=60)
    public int ishCooldown = 10;

    // Añadir ishItem
    @Option("ammo_module.ish_item")
    public SelectableItem.Special ishItem = SelectableItem.Special.ISH_01;

    @Option("ammo_module.pem_desc")
    public String pemDescription = "Condiciones PEM-01";
}
