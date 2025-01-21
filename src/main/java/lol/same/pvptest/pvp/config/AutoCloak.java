package lol.same.pvptest.pvp.config;

import eu.darkbot.api.config.annotations.Configuration;
import eu.darkbot.api.config.annotations.Option;
import eu.darkbot.api.config.annotations.Number;

@Configuration("auto_cloak")
public class AutoCloak {
    @Option("general.enabled")
    public boolean autoCloakShip = false;

    @Option("auto_cloak.waiting_time")
    @Number(min = 0, max = 1000, step = 1)
    public long secondsOfWaiting = 10;

    @Option("auto_cloak.only_pvp")
    public boolean onlyPvpMaps = false;
}
