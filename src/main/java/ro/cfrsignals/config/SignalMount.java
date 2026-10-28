package ro.cfrsignals.config;

import java.util.Locale;

public enum SignalMount {
    MAST,
    DWARF,
    CANTILEVER,
    WALL,
    POLE_INLINE;

    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public boolean hasElevatedSupport() {
        return this == MAST || this == CANTILEVER || this == POLE_INLINE;
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
        // Short-lived development builds called this option pole_side.
        if ("pole_side".equals(name)) {
            return POLE_INLINE;
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
