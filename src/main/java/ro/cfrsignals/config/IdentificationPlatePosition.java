package ro.cfrsignals.config;

import java.util.Locale;

public enum IdentificationPlatePosition {
    HIGH,
    NORMAL,
    LOW;

    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static IdentificationPlatePosition fromSerializedName(
            String name,
            IdentificationPlatePosition fallback
    ) {
        if (name != null) {
            for (IdentificationPlatePosition position : values()) {
                if (position.getSerializedName().equals(name)) {
                    return position;
                }
            }
        }
        return fallback;
    }

    public static IdentificationPlatePosition fromNetworkId(int id) {
        return values()[id];
    }

    public static boolean isValidNetworkId(int id) {
        return id >= 0 && id < values().length;
    }
}
