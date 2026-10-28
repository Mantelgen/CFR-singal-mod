package ro.cfrsignals.network;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import ro.cfrsignals.config.CFRSignalConfiguration.SignalFamily;
import ro.cfrsignals.config.CFRSignalConfiguration;
import ro.cfrsignals.config.AuxiliaryIndicator;
import ro.cfrsignals.config.IdentificationPlatePosition;
import ro.cfrsignals.config.SignalFinish;
import ro.cfrsignals.config.SignalHeadAlignment;
import ro.cfrsignals.config.SignalMount;

final class ApplySignalConfigurationPayloadTest {
    @Test
    void acceptsBoundedEditorValues() {
        var payload = new ApplySignalConfigurationPayload(
                BlockPos.ZERO,
                SignalFamily.THREE_LIGHT,
                true,
                false,
                true,
                "X4-A",
                SignalHeadAlignment.LEFT,
                IdentificationPlatePosition.HIGH
        );

        assertTrue(payload.hasValidValues());
    }

    @Test
    void rejectsInvalidEnumIdsAndPlateText() {
        assertFalse(payload(99, "X4", 0, 0).hasValidValues());
        assertFalse(payload(0, "X4", 99, 0).hasValidValues());
        assertFalse(payload(0, "X4", 0, 99).hasValidValues());
        assertFalse(payload(0, "X4", 0, 0, 99, 0, 0).hasValidValues());
        assertFalse(payload(0, "X4", 0, 0, 0, 99, 0).hasValidValues());
        assertFalse(payload(0, "X4", 0, 0, 0, 0, 99).hasValidValues());
        assertFalse(payload(0, "X4", 0, 0, 0, 0, 0, 0).hasValidValues());
        assertFalse(payload(0, "X4", 0, 0, 0, 0, 0, 6).hasValidValues());
        assertFalse(payload(0, "123456789", 0, 0).hasValidValues());
        assertFalse(payload(0, "X4!", 0, 0).hasValidValues());
    }

    @Test
    void rejectsLegacyDwarfMount() {
        var payload = new ApplySignalConfigurationPayload(
                BlockPos.ZERO,
                SignalFamily.SHUNTING,
                false,
                false,
                true,
                "M2",
                SignalHeadAlignment.CENTER,
                IdentificationPlatePosition.NORMAL,
                SignalMount.DWARF,
                SignalFinish.MODERN_GRAY,
                AuxiliaryIndicator.DIRECTION_F,
                1
        );

        assertFalse(payload.hasValidValues());
    }

    @Test
    void acceptsWallMountedElectricSignal() {
        var payload = new ApplySignalConfigurationPayload(
                BlockPos.ZERO,
                SignalFamily.THREE_LIGHT,
                false,
                false,
                true,
                "W1",
                SignalHeadAlignment.CENTER,
                IdentificationPlatePosition.NORMAL,
                SignalMount.WALL,
                SignalFinish.MODERN_GRAY,
                AuxiliaryIndicator.NONE,
                1
        );

        assertTrue(payload.hasValidValues());
    }

    @Test
    void acceptsBridgeMountedMechanicalSignal() {
        var payload = new ApplySignalConfigurationPayload(
                BlockPos.ZERO,
                SignalFamily.MECHANICAL_SEMAPHORE,
                true,
                true,
                true,
                "M1",
                SignalHeadAlignment.RIGHT,
                IdentificationPlatePosition.NORMAL,
                SignalMount.CANTILEVER,
                SignalFinish.CLASSIC,
                AuxiliaryIndicator.NONE,
                1
        );

        assertTrue(payload.hasValidValues());
    }

    @Test
    void acceptsSignalMountedInlineOnAContinuousPole() {
        var payload = new ApplySignalConfigurationPayload(
                BlockPos.ZERO,
                SignalFamily.THREE_LIGHT,
                true,
                true,
                true,
                "P2",
                SignalHeadAlignment.CENTER,
                IdentificationPlatePosition.NORMAL,
                SignalMount.POLE_INLINE,
                SignalFinish.MODERN_GRAY,
                AuxiliaryIndicator.NONE,
                1
        );

        assertTrue(payload.hasValidValues());
    }

    @Test
    void rejectsFamilyIncompatibleOptions() {
        var payload = new ApplySignalConfigurationPayload(
                BlockPos.ZERO,
                SignalFamily.MECHANICAL_SEMAPHORE,
                true,
                false,
                true,
                "M1",
                SignalHeadAlignment.RIGHT,
                IdentificationPlatePosition.NORMAL,
                SignalMount.DWARF,
                SignalFinish.MODERN_GRAY,
                AuxiliaryIndicator.SPEED_60,
                5
        );

        assertFalse(payload.hasValidValues());
    }

    private static ApplySignalConfigurationPayload payload(
            int familyId,
            String text,
            int mountingId,
            int platePositionId
    ) {
        return new ApplySignalConfigurationPayload(
                BlockPos.ZERO,
                familyId,
                true,
                true,
                true,
                text,
                mountingId,
                platePositionId
        );
    }

    private static ApplySignalConfigurationPayload payload(
            int familyId,
            String text,
            int alignmentId,
            int platePositionId,
            int mountId,
            int finishId,
            int auxiliaryIndicatorId
    ) {
        return payload(
                familyId, text, alignmentId, platePositionId, mountId, finishId,
                auxiliaryIndicatorId, CFRSignalConfiguration.DEFAULT_CANTILEVER_SIGNAL_COUNT
        );
    }

    private static ApplySignalConfigurationPayload payload(
            int familyId,
            String text,
            int alignmentId,
            int platePositionId,
            int mountId,
            int finishId,
            int auxiliaryIndicatorId,
            int cantileverSignalCount
    ) {
        return new ApplySignalConfigurationPayload(
                BlockPos.ZERO,
                familyId,
                true,
                true,
                true,
                text,
                alignmentId,
                platePositionId,
                mountId,
                finishId,
                auxiliaryIndicatorId,
                cantileverSignalCount
        );
    }
}
