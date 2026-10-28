package ro.cfrsignals.config;

import java.util.Locale;

public enum SignalFinish {
    CLASSIC,
    MODERN_GRAY;

    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static SignalFinish fromSerializedName(String name) {
        if (name != null) {
            for (SignalFinish finish : values()) {
                if (finish.getSerializedName().equals(name)) {
                    return finish;
                }
            }
        }
        return CLASSIC;
    }

    public static SignalFinish fromNetworkId(int id) {
        return values()[id];
    }

    public static boolean isValidNetworkId(int id) {
        return id >= 0 && id < values().length;
    }
}
