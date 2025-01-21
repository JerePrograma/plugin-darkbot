package lol.same.pvptest.syncer.messages;

public class PlayerName extends Message{
    private final String name;

    public PlayerName(int fromPlayerId, String name) {
        super(fromPlayerId);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj.getClass() != this.getClass()) return false;
        var other = (PlayerName) obj;
        return super.equals(obj) && name.equals(other.name);
    }

    @Override
    public String debug() {
        return "Nombre " + this.getName();
    }
}
