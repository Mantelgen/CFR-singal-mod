package ro.cfrsignals.config;

import java.util.Locale;

public enum AuxiliaryIndicator {
    NONE("", DisplayColor.WHITE),
    SPEED_30("3", DisplayColor.WHITE),
    SPEED_60("6", DisplayColor.WHITE),
    SPEED_80("8", DisplayColor.WHITE),
    SPEED_100("10", DisplayColor.WHITE),
    NEXT_SPEED_30("3", DisplayColor.YELLOW),
    NEXT_SPEED_60("6", DisplayColor.YELLOW),
    DIRECTION_F("F", DisplayColor.WHITE),
    TRACK_3("3", DisplayColor.GREEN),
    SHORT_DISTANCE("▼", DisplayColor.WHITE),
    LEFT_TRACK("/", DisplayColor.WHITE);

    private final String displayText;
    private final DisplayColor displayColor;

    AuxiliaryIndicator(String displayText, DisplayColor displayColor) {
        this.displayText = displayText;
        this.displayColor = displayColor;
    }

    public String displayText() {
        return displayText;
    }

    public int argbColor() {
        return displayColor.argb;
    }

    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public boolean visible() {
        return this != NONE;
    }

    public static AuxiliaryIndicator fromSerializedName(String name) {
        if (name != null) {
            for (AuxiliaryIndicator indicator : values()) {
                if (indicator.getSerializedName().equals(name)) {
                    return indicator;
                }
            }
        }
        return NONE;
    }

    public static AuxiliaryIndicator fromNetworkId(int id) {
        return values()[id];
    }

    public static boolean isValidNetworkId(int id) {
        return id >= 0 && id < values().length;
    }

    private enum DisplayColor {
        WHITE(0xFFF5F0D5),
        YELLOW(0xFFFFC928),
        GREEN(0xFF4DFF78);

        private final int argb;

        DisplayColor(int argb) {
            this.argb = argb;
        }
    }
}
