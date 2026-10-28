package ro.cfrsignals.config;

import java.util.Locale;
import java.util.Objects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

public final class CFRSignalConfiguration {
    public static final int CURRENT_VERSION = 9;
    public static final int MIN_MAST_HEIGHT = 2;
    public static final int MAX_MAST_HEIGHT = 8;
    public static final int DEFAULT_MAST_HEIGHT = 4;
    public static final int MIN_RENDERED_MAST_HEIGHT = 5;
    public static final int MAX_IDENTIFICATION_TEXT_LENGTH = 8;
    public static final int MIN_CANTILEVER_SIGNAL_COUNT = 1;
    public static final int MAX_CANTILEVER_SIGNAL_COUNT = 1;
    public static final int DEFAULT_CANTILEVER_SIGNAL_COUNT = 1;
    public static final String DEFAULT_IDENTIFICATION_TEXT = "X4";

    private static final String VERSION_KEY = "Version";
    private static final String FAMILY_KEY = "Family";
    private static final String MAST_HEIGHT_KEY = "MastHeight";
    private static final String LADDER_ENABLED_KEY = "LadderEnabled";
    private static final String ELECTRICAL_BOX_ENABLED_KEY = "ElectricalBoxEnabled";
    private static final String IDENTIFICATION_PLATE_ENABLED_KEY = "IdentificationPlateEnabled";
    private static final String IDENTIFICATION_TEXT_KEY = "IdentificationText";
    private static final String IDENTIFICATION_PLATE_POSITION_KEY = "IdentificationPlatePosition";
    private static final String HEAD_ALIGNMENT_KEY = "HeadAlignment";
    private static final String MOUNT_KEY = "Mount";
    private static final String FINISH_KEY = "Finish";
    private static final String AUXILIARY_INDICATOR_KEY = "AuxiliaryIndicator";
    private static final String CANTILEVER_SIGNAL_COUNT_KEY = "CantileverSignalCount";
    private static final String LEGACY_MOUNTING_ORIENTATION_KEY = "MountingOrientation";

    private final int version;
    private final SignalFamily family;
    private final int mastHeight;
    private final boolean ladderEnabled;
    private final boolean electricalBoxEnabled;
    private final boolean identificationPlateEnabled;
    private final String identificationText;
    private final SignalHeadAlignment headAlignment;
    private final IdentificationPlatePosition identificationPlatePosition;
    private final SignalMount mount;
    private final SignalFinish finish;
    private final AuxiliaryIndicator auxiliaryIndicator;
    private final int cantileverSignalCount;

    public CFRSignalConfiguration(int version, SignalFamily family, int mastHeight) {
        this(version, family, mastHeight, true, true, true);
    }

    public CFRSignalConfiguration(
            int version,
            SignalFamily family,
            int mastHeight,
            boolean ladderEnabled,
            boolean electricalBoxEnabled,
            boolean identificationPlateEnabled
    ) {
        this(
                version,
                family,
                mastHeight,
                ladderEnabled,
                electricalBoxEnabled,
                identificationPlateEnabled,
                DEFAULT_IDENTIFICATION_TEXT,
                defaultHeadAlignment(family),
                IdentificationPlatePosition.NORMAL
        );
    }

    public CFRSignalConfiguration(
            int version,
            SignalFamily family,
            int mastHeight,
            boolean ladderEnabled,
            boolean electricalBoxEnabled,
            boolean identificationPlateEnabled,
            String identificationText,
            SignalHeadAlignment headAlignment
    ) {
        this(
                version,
                family,
                mastHeight,
                ladderEnabled,
                electricalBoxEnabled,
                identificationPlateEnabled,
                identificationText,
                headAlignment,
                IdentificationPlatePosition.NORMAL
        );
    }

    public CFRSignalConfiguration(
            int version,
            SignalFamily family,
            int mastHeight,
            boolean ladderEnabled,
            boolean electricalBoxEnabled,
            boolean identificationPlateEnabled,
            String identificationText,
            SignalHeadAlignment headAlignment,
            IdentificationPlatePosition identificationPlatePosition
    ) {
        this(
                version,
                family,
                mastHeight,
                ladderEnabled,
                electricalBoxEnabled,
                identificationPlateEnabled,
                identificationText,
                headAlignment,
                identificationPlatePosition,
                SignalMount.MAST,
                SignalFinish.CLASSIC,
                AuxiliaryIndicator.NONE
        );
    }

    public CFRSignalConfiguration(
            int version,
            SignalFamily family,
            int mastHeight,
            boolean ladderEnabled,
            boolean electricalBoxEnabled,
            boolean identificationPlateEnabled,
            String identificationText,
            SignalHeadAlignment headAlignment,
            IdentificationPlatePosition identificationPlatePosition,
            SignalMount mount,
            SignalFinish finish,
            AuxiliaryIndicator auxiliaryIndicator
    ) {
        this(
                version, family, mastHeight, ladderEnabled, electricalBoxEnabled,
                identificationPlateEnabled, identificationText, headAlignment,
                identificationPlatePosition, mount, finish, auxiliaryIndicator,
                DEFAULT_CANTILEVER_SIGNAL_COUNT
        );
    }

    public CFRSignalConfiguration(
            int version,
            SignalFamily family,
            int mastHeight,
            boolean ladderEnabled,
            boolean electricalBoxEnabled,
            boolean identificationPlateEnabled,
            String identificationText,
            SignalHeadAlignment headAlignment,
            IdentificationPlatePosition identificationPlatePosition,
            SignalMount mount,
            SignalFinish finish,
            AuxiliaryIndicator auxiliaryIndicator,
            int cantileverSignalCount
    ) {
        this.version = CURRENT_VERSION;
        this.family = Objects.requireNonNullElse(family, SignalFamily.TWO_LIGHT);
        this.mastHeight = Mth.clamp(mastHeight, MIN_MAST_HEIGHT, MAX_MAST_HEIGHT);
        this.mount = SignalOptionCompatibility.normalizeMount(this.family, mount);
        this.finish = SignalOptionCompatibility.normalizeFinish(this.family, finish);
        this.auxiliaryIndicator = SignalOptionCompatibility.normalizeIndicator(this.family, auxiliaryIndicator);
        // Mast assemblies physically contain these parts; wall heads do not use them.
        this.ladderEnabled = this.mount.hasElevatedSupport();
        this.electricalBoxEnabled = this.mount.hasElevatedSupport();
        this.identificationPlateEnabled = identificationPlateEnabled
                && SignalOptionCompatibility.supportsIdentificationPlate(this.family);
        this.identificationText = sanitizeIdentificationText(identificationText);
        SignalHeadAlignment requestedAlignment = Objects.requireNonNullElse(
                headAlignment,
                defaultHeadAlignment(this.family)
        );
        this.headAlignment = SignalOptionCompatibility.supportsHeadAlignment(this.family, this.mount)
                ? requestedAlignment
                : defaultHeadAlignment(this.family);
        this.identificationPlatePosition = Objects.requireNonNullElse(
                identificationPlatePosition,
                IdentificationPlatePosition.NORMAL
        );
        // A controller represents one independently controlled railway signal.
        // Version 8 allowed several duplicate visual heads on one cantilever;
        // normalize those worlds to a single head instead of implying that the
        // copies could protect separate tracks.
        this.cantileverSignalCount = DEFAULT_CANTILEVER_SIGNAL_COUNT;
    }

    public static CFRSignalConfiguration defaults() {
        return new CFRSignalConfiguration(CURRENT_VERSION, SignalFamily.TWO_LIGHT, DEFAULT_MAST_HEIGHT);
    }

    public int version() {
        return version;
    }

    public SignalFamily family() {
        return family;
    }

    public int mastHeight() {
        return mastHeight;
    }

    public int renderedMastHeight() {
        // Preserve configured height differences while lowering every visual by two blocks.
        int baseHeight = Math.max(mastHeight - 1, MIN_RENDERED_MAST_HEIGHT);
        return family == SignalFamily.MECHANICAL_SEMAPHORE ? baseHeight + 1 : baseHeight;
    }

    public boolean ladderEnabled() {
        return ladderEnabled;
    }

    public boolean electricalBoxEnabled() {
        return electricalBoxEnabled;
    }

    public boolean identificationPlateEnabled() {
        return identificationPlateEnabled;
    }

    public String identificationText() {
        return identificationText;
    }

    public SignalHeadAlignment headAlignment() {
        return headAlignment;
    }

    public IdentificationPlatePosition identificationPlatePosition() {
        return identificationPlatePosition;
    }

    public SignalMount mount() {
        return mount;
    }

    public SignalFinish finish() {
        return finish;
    }

    public AuxiliaryIndicator auxiliaryIndicator() {
        return auxiliaryIndicator;
    }

    public int cantileverSignalCount() {
        return cantileverSignalCount;
    }

    public CFRSignalConfiguration withFamily(SignalFamily family) {
        return new CFRSignalConfiguration(
                CURRENT_VERSION,
                family,
                mastHeight,
                ladderEnabled,
                electricalBoxEnabled,
                identificationPlateEnabled,
                identificationText,
                headAlignment,
                identificationPlatePosition,
                mount,
                finish,
                auxiliaryIndicator,
                cantileverSignalCount
        );
    }

    public CFRSignalConfiguration withLadderEnabled(boolean enabled) {
        return copy(enabled, electricalBoxEnabled, identificationPlateEnabled);
    }

    public CFRSignalConfiguration withElectricalBoxEnabled(boolean enabled) {
        return copy(ladderEnabled, enabled, identificationPlateEnabled);
    }

    public CFRSignalConfiguration withIdentificationPlateEnabled(boolean enabled) {
        return copy(ladderEnabled, electricalBoxEnabled, enabled);
    }

    public CFRSignalConfiguration withIdentificationText(String text) {
        return new CFRSignalConfiguration(
                CURRENT_VERSION,
                family,
                mastHeight,
                ladderEnabled,
                electricalBoxEnabled,
                identificationPlateEnabled,
                text,
                headAlignment,
                identificationPlatePosition,
                mount,
                finish,
                auxiliaryIndicator,
                cantileverSignalCount
        );
    }

    public CFRSignalConfiguration withHeadAlignment(SignalHeadAlignment alignment) {
        return new CFRSignalConfiguration(
                CURRENT_VERSION,
                family,
                mastHeight,
                ladderEnabled,
                electricalBoxEnabled,
                identificationPlateEnabled,
                identificationText,
                alignment,
                identificationPlatePosition,
                mount,
                finish,
                auxiliaryIndicator,
                cantileverSignalCount
        );
    }

    public CFRSignalConfiguration withIdentificationPlatePosition(IdentificationPlatePosition position) {
        return new CFRSignalConfiguration(
                CURRENT_VERSION,
                family,
                mastHeight,
                ladderEnabled,
                electricalBoxEnabled,
                identificationPlateEnabled,
                identificationText,
                headAlignment,
                position,
                mount,
                finish,
                auxiliaryIndicator,
                cantileverSignalCount
        );
    }

    public CFRSignalConfiguration withMount(SignalMount mount) {
        return new CFRSignalConfiguration(
                CURRENT_VERSION, family, mastHeight, ladderEnabled, electricalBoxEnabled,
                identificationPlateEnabled, identificationText, headAlignment,
                identificationPlatePosition, mount, finish, auxiliaryIndicator, cantileverSignalCount
        );
    }

    public CFRSignalConfiguration withFinish(SignalFinish finish) {
        return new CFRSignalConfiguration(
                CURRENT_VERSION, family, mastHeight, ladderEnabled, electricalBoxEnabled,
                identificationPlateEnabled, identificationText, headAlignment,
                identificationPlatePosition, mount, finish, auxiliaryIndicator, cantileverSignalCount
        );
    }

    public CFRSignalConfiguration withAuxiliaryIndicator(AuxiliaryIndicator auxiliaryIndicator) {
        return new CFRSignalConfiguration(
                CURRENT_VERSION, family, mastHeight, ladderEnabled, electricalBoxEnabled,
                identificationPlateEnabled, identificationText, headAlignment,
                identificationPlatePosition, mount, finish, auxiliaryIndicator, cantileverSignalCount
        );
    }

    public CFRSignalConfiguration withCantileverSignalCount(int count) {
        return new CFRSignalConfiguration(
                CURRENT_VERSION, family, mastHeight, ladderEnabled, electricalBoxEnabled,
                identificationPlateEnabled, identificationText, headAlignment,
                identificationPlatePosition, mount, finish, auxiliaryIndicator, count
        );
    }

    public CFRSignalConfiguration withEditorSettings(
            SignalFamily family,
            boolean ladderEnabled,
            boolean electricalBoxEnabled,
            boolean identificationPlateEnabled,
            String identificationText,
            SignalHeadAlignment headAlignment,
            IdentificationPlatePosition identificationPlatePosition
    ) {
        return withEditorSettings(
                family,
                ladderEnabled,
                electricalBoxEnabled,
                identificationPlateEnabled,
                identificationText,
                headAlignment,
                identificationPlatePosition,
                mount,
                finish,
                auxiliaryIndicator,
                cantileverSignalCount
        );
    }

    public CFRSignalConfiguration withEditorSettings(
            SignalFamily family,
            boolean ladderEnabled,
            boolean electricalBoxEnabled,
            boolean identificationPlateEnabled,
            String identificationText,
            SignalHeadAlignment headAlignment,
            IdentificationPlatePosition identificationPlatePosition,
            SignalMount mount,
            SignalFinish finish,
            AuxiliaryIndicator auxiliaryIndicator
    ) {
        return withEditorSettings(
                family, ladderEnabled, electricalBoxEnabled, identificationPlateEnabled,
                identificationText, headAlignment, identificationPlatePosition, mount,
                finish, auxiliaryIndicator, cantileverSignalCount
        );
    }

    public CFRSignalConfiguration withEditorSettings(
            SignalFamily family,
            boolean ladderEnabled,
            boolean electricalBoxEnabled,
            boolean identificationPlateEnabled,
            String identificationText,
            SignalHeadAlignment headAlignment,
            IdentificationPlatePosition identificationPlatePosition,
            SignalMount mount,
            SignalFinish finish,
            AuxiliaryIndicator auxiliaryIndicator,
            int cantileverSignalCount
    ) {
        return new CFRSignalConfiguration(
                CURRENT_VERSION,
                family,
                mastHeight,
                ladderEnabled,
                electricalBoxEnabled,
                identificationPlateEnabled,
                identificationText,
                headAlignment,
                identificationPlatePosition,
                mount,
                finish,
                auxiliaryIndicator,
                cantileverSignalCount
        );
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(VERSION_KEY, CURRENT_VERSION);
        tag.putString(FAMILY_KEY, family.getSerializedName());
        tag.putInt(MAST_HEIGHT_KEY, mastHeight);
        tag.putBoolean(LADDER_ENABLED_KEY, ladderEnabled);
        tag.putBoolean(ELECTRICAL_BOX_ENABLED_KEY, electricalBoxEnabled);
        tag.putBoolean(IDENTIFICATION_PLATE_ENABLED_KEY, identificationPlateEnabled);
        tag.putString(IDENTIFICATION_TEXT_KEY, identificationText);
        tag.putString(HEAD_ALIGNMENT_KEY, headAlignment.getSerializedName());
        tag.putString(IDENTIFICATION_PLATE_POSITION_KEY, identificationPlatePosition.getSerializedName());
        tag.putString(MOUNT_KEY, mount.getSerializedName());
        tag.putString(FINISH_KEY, finish.getSerializedName());
        tag.putString(AUXILIARY_INDICATOR_KEY, auxiliaryIndicator.getSerializedName());
        tag.putInt(CANTILEVER_SIGNAL_COUNT_KEY, cantileverSignalCount);
        return tag;
    }

    public static CFRSignalConfiguration read(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return defaults();
        }

        int storedVersion = tag.contains(VERSION_KEY) ? tag.getInt(VERSION_KEY) : 0;
        if (storedVersion > CURRENT_VERSION) {
            return defaults();
        }

        SignalFamily family = SignalFamily.fromSerializedName(tag.getString(FAMILY_KEY));
        int mastHeight = tag.contains(MAST_HEIGHT_KEY) ? tag.getInt(MAST_HEIGHT_KEY) : DEFAULT_MAST_HEIGHT;
        boolean ladderEnabled = readBooleanOrDefault(tag, LADDER_ENABLED_KEY, true);
        boolean electricalBoxEnabled = readBooleanOrDefault(tag, ELECTRICAL_BOX_ENABLED_KEY, true);
        boolean identificationPlateEnabled = readBooleanOrDefault(tag, IDENTIFICATION_PLATE_ENABLED_KEY, true);
        String identificationText = tag.contains(IDENTIFICATION_TEXT_KEY)
                ? tag.getString(IDENTIFICATION_TEXT_KEY)
                : DEFAULT_IDENTIFICATION_TEXT;
        SignalHeadAlignment fallbackAlignment = defaultHeadAlignment(family);
        SignalHeadAlignment headAlignment = tag.contains(HEAD_ALIGNMENT_KEY)
                ? SignalHeadAlignment.fromSerializedName(tag.getString(HEAD_ALIGNMENT_KEY), fallbackAlignment)
                : SignalHeadAlignment.fromSerializedName(
                        tag.getString(LEGACY_MOUNTING_ORIENTATION_KEY),
                        fallbackAlignment
                );
        IdentificationPlatePosition identificationPlatePosition = tag.contains(IDENTIFICATION_PLATE_POSITION_KEY)
                ? IdentificationPlatePosition.fromSerializedName(
                        tag.getString(IDENTIFICATION_PLATE_POSITION_KEY),
                        IdentificationPlatePosition.NORMAL
                )
                : IdentificationPlatePosition.NORMAL;
        SignalMount mount = tag.contains(MOUNT_KEY)
                ? SignalMount.fromSerializedName(tag.getString(MOUNT_KEY))
                : SignalMount.MAST;
        SignalFinish finish = tag.contains(FINISH_KEY)
                ? SignalFinish.fromSerializedName(tag.getString(FINISH_KEY))
                : SignalFinish.CLASSIC;
        AuxiliaryIndicator auxiliaryIndicator = tag.contains(AUXILIARY_INDICATOR_KEY)
                ? AuxiliaryIndicator.fromSerializedName(tag.getString(AUXILIARY_INDICATOR_KEY))
                : AuxiliaryIndicator.NONE;
        int cantileverSignalCount = tag.contains(CANTILEVER_SIGNAL_COUNT_KEY)
                ? tag.getInt(CANTILEVER_SIGNAL_COUNT_KEY)
                : DEFAULT_CANTILEVER_SIGNAL_COUNT;
        return new CFRSignalConfiguration(
                CURRENT_VERSION,
                family,
                mastHeight,
                ladderEnabled,
                electricalBoxEnabled,
                identificationPlateEnabled,
                identificationText,
                headAlignment,
                identificationPlatePosition,
                mount,
                finish,
                auxiliaryIndicator,
                cantileverSignalCount
        );
    }

    private CFRSignalConfiguration copy(
            boolean ladderEnabled,
            boolean electricalBoxEnabled,
            boolean identificationPlateEnabled
    ) {
        return new CFRSignalConfiguration(
                CURRENT_VERSION,
                family,
                mastHeight,
                ladderEnabled,
                electricalBoxEnabled,
                identificationPlateEnabled,
                identificationText,
                headAlignment,
                identificationPlatePosition,
                mount,
                finish,
                auxiliaryIndicator,
                cantileverSignalCount
        );
    }

    public static String sanitizeIdentificationText(String text) {
        String input = text == null ? DEFAULT_IDENTIFICATION_TEXT : text.strip();
        StringBuilder sanitized = new StringBuilder(MAX_IDENTIFICATION_TEXT_LENGTH);
        input.codePoints()
                .filter(codePoint -> Character.isLetterOrDigit(codePoint)
                        || codePoint == ' '
                        || codePoint == '-')
                .takeWhile(codePoint -> sanitized.length() + Character.charCount(codePoint)
                        <= MAX_IDENTIFICATION_TEXT_LENGTH)
                .forEach(sanitized::appendCodePoint);
        return sanitized.toString();
    }

    public static boolean isValidIdentificationText(String text) {
        return text != null
                && text.length() <= MAX_IDENTIFICATION_TEXT_LENGTH
                && sanitizeIdentificationText(text).equals(text);
    }

    private static boolean readBooleanOrDefault(CompoundTag tag, String key, boolean defaultValue) {
        return tag.contains(key) ? tag.getBoolean(key) : defaultValue;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CFRSignalConfiguration that)) {
            return false;
        }
        return version == that.version
                && mastHeight == that.mastHeight
                && ladderEnabled == that.ladderEnabled
                && electricalBoxEnabled == that.electricalBoxEnabled
                && identificationPlateEnabled == that.identificationPlateEnabled
                && identificationText.equals(that.identificationText)
                && headAlignment == that.headAlignment
                && identificationPlatePosition == that.identificationPlatePosition
                && mount == that.mount
                && finish == that.finish
                && auxiliaryIndicator == that.auxiliaryIndicator
                && cantileverSignalCount == that.cantileverSignalCount
                && family == that.family;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                version,
                family,
                mastHeight,
                ladderEnabled,
                electricalBoxEnabled,
                identificationPlateEnabled,
                identificationText,
                headAlignment,
                identificationPlatePosition,
                mount,
                finish,
                auxiliaryIndicator,
                cantileverSignalCount
        );
    }

    public enum SignalFamily {
        TWO_LIGHT(false),
        THREE_LIGHT(true),
        SHUNTING(false),
        REPEATER(true),
        TMV(true),
        EMERGENCY(false),
        LEGACY_DWARF_ENTRY(false),
        MECHANICAL_SEMAPHORE(false);

        private final boolean supportsYellow;

        SignalFamily(boolean supportsYellow) {
            this.supportsYellow = supportsYellow;
        }

        public boolean supportsYellow() {
            return supportsYellow;
        }

        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public static SignalFamily fromSerializedName(String name) {
            if (name != null) {
                for (SignalFamily family : values()) {
                    if (family.getSerializedName().equals(name)) {
                        return family;
                    }
                }
            }
            return TWO_LIGHT;
        }

        public static SignalFamily fromNetworkId(int id) {
            return values()[id];
        }

        public static boolean isValidNetworkId(int id) {
            return id >= 0 && id < values().length;
        }
    }

    public static SignalHeadAlignment defaultHeadAlignment(SignalFamily family) {
        return family == SignalFamily.MECHANICAL_SEMAPHORE
                ? SignalHeadAlignment.RIGHT
                : SignalHeadAlignment.CENTER;
    }
}
