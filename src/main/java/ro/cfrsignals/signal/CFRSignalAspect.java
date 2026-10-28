package ro.cfrsignals.signal;

import java.util.Locale;

public enum CFRSignalAspect {
    OFF,
    RED,
    YELLOW,
    GREEN;

    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String getTranslationKey() {
        return "aspect.cfrsignals." + getSerializedName();
    }

    public CFRSignalAspect nextDebugAspect(boolean supportsYellow) {
        if (!supportsYellow) {
            return this == RED ? GREEN : RED;
        }
        return switch (this) {
            case RED -> YELLOW;
            case YELLOW -> GREEN;
            default -> RED;
        };
    }

    public static CFRSignalAspect fromSerializedName(String name) {
        if (name != null) {
            for (CFRSignalAspect aspect : values()) {
                if (aspect.getSerializedName().equals(name)) {
                    return aspect;
                }
            }
        }
        return RED;
    }
}
