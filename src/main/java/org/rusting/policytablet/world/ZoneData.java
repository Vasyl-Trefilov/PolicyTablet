package org.rusting.policytablet.world;

import java.util.HashMap;
import java.util.Map;

public class ZoneData {
    private static final Map<String, String> OWNERS = new HashMap<>();

    public static String getOwner(String cellLabel) {
        return OWNERS.getOrDefault(cellLabel, "neutral");
    }

    public static void setOwner(String cellLabel, String country) {
        OWNERS.put(cellLabel, country);
    }

    public static Map<String, String> getAllOwners() {
        return new HashMap<>(OWNERS);
    }
}
