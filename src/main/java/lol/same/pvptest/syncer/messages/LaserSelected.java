package lol.same.pvptest.syncer.messages;

import eu.darkbot.api.game.items.SelectableItem;

public class LaserSelected extends Message {
    private final String laserId;

    public LaserSelected(int fromPlayerId, SelectableItem.Laser laser) {
        super(fromPlayerId);
        this.laserId = laser.getId();
    }

    public SelectableItem.Laser getLaser() {
        return SelectableItem.Laser.of(laserId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj.getClass() != this.getClass()) return false;
        var other = (LaserSelected) obj;
        return super.equals(obj) && laserId.equals(other.laserId);
    }

    @Override
    public String debug() {
        return "Laser " + SelectableItem.Laser.of(laserId);
    }
}
