package lol.same.pvptest;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PlayerNames {
    static final Map<Integer, String> names = new HashMap<>();

    public static void setName(int playerId, String name) {
        names.put(playerId, name);
    }

    public static String getName(int playerId) {
        return Optional.ofNullable(names.get(playerId)).orElse("" + playerId);
    }
}
