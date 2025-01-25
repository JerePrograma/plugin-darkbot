package lol.same.pvptest.pvp.config;

import eu.darkbot.api.config.annotations.Configuration;
import eu.darkbot.api.config.annotations.Option;
import eu.darkbot.api.config.annotations.Number;

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
}
