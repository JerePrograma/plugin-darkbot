package lol.same.pvptest.syncer.messages;

import eu.darkbot.api.game.other.GameMap;
import eu.darkbot.api.game.other.Location;

public class MyLocation extends Message {
    private final double x;
    private final double y;
    private final int mapId;

    public MyLocation(int fromPlayerId, Location location, GameMap gameMap) {
        super(fromPlayerId);
        this.x = location.getX();
        this.y = location.getY();
        this.mapId = gameMap.getId();
    }

    public Location getLocation() {
        return Location.of(x, y);
    }

    public int getMapId() {
        return mapId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj.getClass() != this.getClass()) return false;
        var other = (MyLocation) obj;
        return super.equals(obj) && x == other.x && y == other.y && mapId == other.mapId;
    }

    @Override
    public String debug() {
        return "Mapa " + mapId;
    }
}
