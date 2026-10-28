package ro.cfrsignals.config;

import java.util.Locale;

public enum SignalMount {
    MAST,
    DWARF,
    CANTILEVER,
    WALL;

    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public boolean hasElevatedSupport() {
        return this == MAST || this == CANTILEVER;
    }

    public boolean suspendsHead() {
        return this == CANTILEVER;
    }

    public static SignalMount fromSerializedName(String name) {
        // Version 6 worlds used a separate gantry option. Preserve those builds
        // as single-head cantilevers.
        if ("gantry".equals(name)) {
            return CANTILEVER;
        }
        if (name != null) {
            for (SignalMount mount : values()) {
                if (mount.getSerializedName().equals(name)) {
                    return mount;
                }
            }
        }
        return MAST;
    }

    public static SignalMount fromNetworkId(int id) {
        return values()[id];
    }

    public static boolean isValidNetworkId(int id) {
        return id >= 0 && id < values().length;
    }
}
