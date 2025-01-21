package lol.same.pvptest.utils;

import java.util.HashMap;
import java.util.Map;

public class LogIfChanged {
    private static final Map<String, String> messages = new HashMap<>();

    public static void log(String key, String value) {
        var prev = messages.put(key, value);
        if (!value.equals(prev))
            System.out.println(key + ": " + value);
    }

    private LogIfChanged() {}
}
