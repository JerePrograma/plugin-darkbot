package lol.same.pvptest.syncer.messages;

import java.io.Serializable;

public abstract class Message implements Serializable {
    public int fromPlayerId;

    public Message(int fromPlayerId) {
        this.fromPlayerId = fromPlayerId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj.getClass() != this.getClass()) return false;
        var other = (Message) obj;
        return fromPlayerId == other.fromPlayerId;
    }

    public abstract String debug();
}
