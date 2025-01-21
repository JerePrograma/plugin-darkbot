package lol.same.pvptest.syncer.messages;

import java.util.Optional;

public class MyLeaderPriority extends Message {
    private final boolean canBeLeader;
    private final int priority;

    public MyLeaderPriority(int fromPlayerId, boolean canBeLeader, int priority) {
        super(fromPlayerId);
        this.canBeLeader = canBeLeader;
        this.priority = priority;
    }

    public Optional<Integer> getPriority() {
        return canBeLeader ? Optional.of(priority) : Optional.empty();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj.getClass() != this.getClass()) return false;
        var other = (MyLeaderPriority) obj;
        return super.equals(obj) && (canBeLeader
                ? (other.canBeLeader && priority == other.priority)
                : !other.canBeLeader);
    }

    @Override
    public String debug() {
        return "Prioridad como líder " + this.canBeLeader + " " + this.priority;
    }
}
