package lol.same.pvptest.syncer.messages;

import eu.darkbot.api.game.items.SelectableItem;

public class RocketSelected extends Message {
    private final String rocketId;

    public RocketSelected(int fromPlayerId, SelectableItem.Rocket rocket) {
        super(fromPlayerId);
        this.rocketId = rocket.getId();
    }

    public SelectableItem.Rocket getRocket() {
        return SelectableItem.Rocket.of(rocketId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj.getClass() != this.getClass()) return false;
        var other = (RocketSelected) obj;
        return super.equals(obj) && rocketId.equals(other.rocketId);
    }

    @Override
    public String debug() {
        return "Rocket " + SelectableItem.Rocket.of(rocketId);
    }
}
