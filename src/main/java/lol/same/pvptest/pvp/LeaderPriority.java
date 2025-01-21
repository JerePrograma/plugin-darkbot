package lol.same.pvptest.pvp;

import lol.same.pvptest.utils.LogIfChanged;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LeaderPriority {
    private static final HashMap<Integer, Integer> leaderPriorities = new HashMap<>();
    private static int myId;
    private static boolean canBeLeader;
    private static int myPriority;

    public static void setMyId(int id) {
        if (id == 0 || id == -1 || id == myId) return;
        myId = id;
        setPriority(myId, canBeLeader, myPriority);
    }

    public static void setPriority(int playerId, boolean canBeLeader, int priority) {
        LogIfChanged.log("Prioridad de " + playerId, canBeLeader + " " + priority);
        if (!canBeLeader)
            leaderPriorities.remove(playerId);
        else
            leaderPriorities.put(playerId, priority);
    }

    public static void setMyPriority(boolean canBeLeader, int priority) {
        LeaderPriority.canBeLeader = canBeLeader;
        LeaderPriority.myPriority = priority;
        if (myId != 0) setPriority(myId, canBeLeader, priority);
    }

    public static Optional<Integer> getPriority(int playerId) {
        return Optional.ofNullable(leaderPriorities.get(playerId));
    }

    public static Optional<Integer> getMyPriority() {
        return getPriority(myId);
    }

    public static Optional<Integer> getHighestPriorityId() {
        return leaderPriorities.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }
}
