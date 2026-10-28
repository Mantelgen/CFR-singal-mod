package ro.cfrsignals.config;

import java.util.List;

import ro.cfrsignals.config.CFRSignalConfiguration.SignalFamily;

/** Defines combinations that correspond to plausible CFR signal installations. */
public final class SignalOptionCompatibility {
    private static final List<SignalMount> PHYSICAL_ASSEMBLY_MOUNT = List.of(SignalMount.MAST);
    private static final List<SignalMount> ELECTRIC_MOUNTS = List.of(
            SignalMount.MAST,
            SignalMount.POLE_INLINE,
            SignalMount.CANTILEVER,
            SignalMount.WALL
    );
    private static final List<SignalMount> ELEVATED_MOUNTS = List.of(
            SignalMount.MAST,
            SignalMount.POLE_INLINE,
            SignalMount.CANTILEVER
    );
    private static final List<SignalFinish> ALL_FINISHES = List.of(SignalFinish.values());
    private static final List<AuxiliaryIndicator> ALL_INDICATORS = List.of(AuxiliaryIndicator.values());
    private static final List<AuxiliaryIndicator> SHUNTING_INDICATORS = List.of(
            AuxiliaryIndicator.NONE,
            AuxiliaryIndicator.DIRECTION_F,
            AuxiliaryIndicator.TRACK_3,
            AuxiliaryIndicator.LEFT_TRACK
    );

    private SignalOptionCompatibility() {
    }

    public static List<SignalMount> allowedMounts(SignalFamily family) {
        return switch (safeFamily(family)) {
            case TWO_LIGHT, THREE_LIGHT, SHUNTING, REPEATER, TMV -> ELECTRIC_MOUNTS;
            case EMERGENCY, MECHANICAL_SEMAPHORE -> ELEVATED_MOUNTS;
            case LEGACY_DWARF_ENTRY -> PHYSICAL_ASSEMBLY_MOUNT;
        };
    }

    public static List<SignalFinish> allowedFinishes(SignalFamily family) {
        return switch (safeFamily(family)) {
            case LEGACY_DWARF_ENTRY, MECHANICAL_SEMAPHORE -> List.of(SignalFinish.CLASSIC);
            default -> ALL_FINISHES;
        };
    }

    public static List<AuxiliaryIndicator> allowedIndicators(SignalFamily family) {
        return switch (safeFamily(family)) {
            case TWO_LIGHT, THREE_LIGHT, TMV -> ALL_INDICATORS;
            case SHUNTING -> SHUNTING_INDICATORS;
            case REPEATER, EMERGENCY, LEGACY_DWARF_ENTRY, MECHANICAL_SEMAPHORE -> List.of(
                    AuxiliaryIndicator.NONE
            );
        };
    }

    public static SignalMount normalizeMount(SignalFamily family, SignalMount requested) {
        List<SignalMount> allowed = allowedMounts(family);
        return allowed.contains(requested) ? requested : allowed.getFirst();
    }

    public static SignalFinish normalizeFinish(SignalFamily family, SignalFinish requested) {
        List<SignalFinish> allowed = allowedFinishes(family);
        return allowed.contains(requested) ? requested : allowed.getFirst();
    }

    public static AuxiliaryIndicator normalizeIndicator(
            SignalFamily family,
            AuxiliaryIndicator requested
    ) {
        List<AuxiliaryIndicator> allowed = allowedIndicators(family);
        return allowed.contains(requested) ? requested : allowed.getFirst();
    }

    public static boolean supportsLadder(SignalFamily family, SignalMount mount) {
        return mount.hasElevatedSupport();
    }

    public static boolean supportsElectricalBox(SignalFamily family, SignalMount mount) {
        return mount.hasElevatedSupport();
    }

    public static boolean supportsIdentificationPlate(SignalFamily family) {
        return safeFamily(family) != SignalFamily.EMERGENCY;
    }

    public static boolean supportsHeadAlignment(SignalFamily family, SignalMount mount) {
        return safeFamily(family) != SignalFamily.MECHANICAL_SEMAPHORE
                && safeFamily(family) != SignalFamily.EMERGENCY
                && mount == SignalMount.MAST;
    }

    private static SignalFamily safeFamily(SignalFamily family) {
        return family == null ? SignalFamily.TWO_LIGHT : family;
    }
}
