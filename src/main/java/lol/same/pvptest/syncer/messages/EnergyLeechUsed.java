package lol.same.pvptest.syncer.messages;

public class EnergyLeechUsed extends Message {
    public EnergyLeechUsed(int fromPlayerId) {
        super(fromPlayerId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj.getClass() != this.getClass()) return false;
        return super.equals(obj);
    }

    @Override
    public String debug() {
        return "Energy Leech usado";
    }
}
