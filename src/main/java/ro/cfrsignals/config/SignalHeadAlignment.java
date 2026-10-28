package ro.cfrsignals.config;

import java.util.Locale;

public enum SignalHeadAlignment {
    LEFT,
    CENTER,
    RIGHT;

    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static SignalHeadAlignment fromSerializedName(String name, SignalHeadAlignment fallback) {
        if (name != null) {
            for (SignalHeadAlignment alignment : values()) {
                if (alignment.getSerializedName().equals(name)) {
                    return alignment;
                }
            }
        }
        return fallback;
    }

    public static SignalHeadAlignment fromNetworkId(int id) {
        return values()[id];
    }

    public static boolean isValidNetworkId(int id) {
        return id >= 0 && id < values().length;
    }
}
