package lol.same.pvptest.utils;

import eu.darkbot.api.game.other.Location;
import org.jetbrains.annotations.Nullable;

public class LocationAndMap {
    public @Nullable Location location;
    public int mapId;

    public LocationAndMap(@Nullable Location location, int mapId) {
        this.location = location;
        this.mapId = mapId;
    }
}
