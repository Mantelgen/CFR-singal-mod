package ro.cfrsignals.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import ro.cfrsignals.config.CFRSignalConfiguration.SignalFamily;

final class CFRSignalConfigurationTest {
    @Test
    void nbtRoundTripPreservesConfiguration() {
        CFRSignalConfiguration original = new CFRSignalConfiguration(
                CFRSignalConfiguration.CURRENT_VERSION,
                SignalFamily.TWO_LIGHT,
                6,
                false,
                true,
                false
        );

        CFRSignalConfiguration restored = CFRSignalConfiguration.read(original.write());

        assertEquals(original, restored);
    }

    @Test
    void missingDataUsesStableDefaults() {
        assertEquals(CFRSignalConfiguration.defaults(), CFRSignalConfiguration.read(new CompoundTag()));
        assertEquals(SignalHeadAlignment.CENTER, CFRSignalConfiguration.defaults().headAlignment());
        assertEquals(
                IdentificationPlatePosition.NORMAL,
                CFRSignalConfiguration.defaults().identificationPlatePosition()
        );
    }

    @Test
    void legacyMountingOrientationMigratesWithoutMovingExistingHead() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Version", 3);
        tag.putString("Family", SignalFamily.THREE_LIGHT.getSerializedName());
        tag.putString("MountingOrientation", "right");

        CFRSignalConfiguration restored = CFRSignalConfiguration.read(tag);

        assertEquals(SignalHeadAlignment.RIGHT, restored.headAlignment());
    }

    @Test
    void unsupportedFutureVersionFallsBackSafely() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Version", CFRSignalConfiguration.CURRENT_VERSION + 1);
        tag.putString("Family", SignalFamily.TWO_LIGHT.getSerializedName());
        tag.putInt("MastHeight", 8);

        assertEquals(CFRSignalConfiguration.defaults(), CFRSignalConfiguration.read(tag));
    }

    @Test
    void legacyConfigurationEnablesNewAttachmentsByDefault() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Version", 1);
        tag.putString("Family", SignalFamily.TWO_LIGHT.getSerializedName());
        tag.putInt("MastHeight", 5);

        CFRSignalConfiguration restored = CFRSignalConfiguration.read(tag);

        assertEquals(true, restored.ladderEnabled());
        assertEquals(true, restored.electricalBoxEnabled());
        assertEquals(true, restored.identificationPlateEnabled());
    }

    @Test
    void attachmentCopiesPreserveOtherConfiguration() {
        CFRSignalConfiguration configuration = CFRSignalConfiguration.defaults()
                .withLadderEnabled(false)
                .withElectricalBoxEnabled(false)
                .withIdentificationPlateEnabled(false);

        assertEquals(
                new CFRSignalConfiguration(
                        CFRSignalConfiguration.CURRENT_VERSION,
                        SignalFamily.TWO_LIGHT,
                        CFRSignalConfiguration.DEFAULT_MAST_HEIGHT,
                        false,
                        false,
                        false
                ),
                configuration
        );
    }

    @Test
    void threeLightFamilyRoundTripsAndPreservesAttachments() {
        CFRSignalConfiguration configuration = CFRSignalConfiguration.defaults()
                .withLadderEnabled(false)
                .withElectricalBoxEnabled(false)
                .withFamily(SignalFamily.THREE_LIGHT);

        CFRSignalConfiguration restored = CFRSignalConfiguration.read(configuration.write());

        assertEquals(SignalFamily.THREE_LIGHT, restored.family());
        assertEquals(true, restored.ladderEnabled());
        assertEquals(true, restored.electricalBoxEnabled());
        assertEquals(true, restored.identificationPlateEnabled());
        assertEquals(CFRSignalConfiguration.DEFAULT_MAST_HEIGHT, restored.mastHeight());
    }

    @Test
    void editorSettingsRoundTrip() {
        CFRSignalConfiguration configuration = CFRSignalConfiguration.defaults().withEditorSettings(
                SignalFamily.TMV,
                false,
                false,
                true,
                "B-12",
                SignalHeadAlignment.LEFT,
                IdentificationPlatePosition.LOW,
                SignalMount.CANTILEVER,
                SignalFinish.MODERN_GRAY,
                AuxiliaryIndicator.NEXT_SPEED_60,
                5
        );

        CFRSignalConfiguration restored = CFRSignalConfiguration.read(configuration.write());

        assertEquals(configuration, restored);
        assertEquals("B-12", restored.identificationText());
        assertEquals(SignalHeadAlignment.CENTER, restored.headAlignment());
        assertEquals(IdentificationPlatePosition.LOW, restored.identificationPlatePosition());
        assertEquals(SignalMount.CANTILEVER, restored.mount());
        assertEquals(SignalFinish.MODERN_GRAY, restored.finish());
        assertEquals(AuxiliaryIndicator.NEXT_SPEED_60, restored.auxiliaryIndicator());
        assertEquals(1, restored.cantileverSignalCount());
    }

    @Test
    void wallConfigurationKeepsWallMountAndDropsMastOnlyParts() {
        CFRSignalConfiguration configuration = CFRSignalConfiguration.defaults().withEditorSettings(
                SignalFamily.THREE_LIGHT,
                true,
                true,
                true,
                "W-3",
                SignalHeadAlignment.LEFT,
                IdentificationPlatePosition.HIGH,
                SignalMount.WALL,
                SignalFinish.MODERN_GRAY,
                AuxiliaryIndicator.SPEED_60,
                1
        );

        assertEquals(SignalMount.WALL, configuration.mount());
        assertEquals(false, configuration.ladderEnabled());
        assertEquals(false, configuration.electricalBoxEnabled());
        assertEquals(SignalHeadAlignment.CENTER, configuration.headAlignment());
        assertEquals(AuxiliaryIndicator.SPEED_60, configuration.auxiliaryIndicator());
    }

    @Test
    void continuousPoleInlineMountRoundTripsAsAnElevatedSupport() {
        CFRSignalConfiguration configuration = CFRSignalConfiguration.defaults().withEditorSettings(
                SignalFamily.THREE_LIGHT,
                true,
                true,
                true,
                "P-2",
                SignalHeadAlignment.LEFT,
                IdentificationPlatePosition.NORMAL,
                SignalMount.POLE_INLINE,
                SignalFinish.MODERN_GRAY,
                AuxiliaryIndicator.NONE,
                1
        );

        CFRSignalConfiguration restored = CFRSignalConfiguration.read(configuration.write());

        assertEquals(SignalMount.POLE_INLINE, restored.mount());
        assertEquals(true, restored.ladderEnabled());
        assertEquals(true, restored.electricalBoxEnabled());
        assertEquals(SignalHeadAlignment.CENTER, restored.headAlignment());
    }

    @Test
    void shortLivedSideMountNameMigratesToInlineMount() {
        CompoundTag tag = CFRSignalConfiguration.defaults().write();
        tag.putString("Mount", "pole_side");

        assertEquals(SignalMount.POLE_INLINE, CFRSignalConfiguration.read(tag).mount());
    }

    @Test
    void preVariationConfigurationUsesOriginalVisualDefaults() {
        CompoundTag tag = CFRSignalConfiguration.defaults().write();
        tag.putInt("Version", 5);
        tag.remove("Mount");
        tag.remove("Finish");
        tag.remove("AuxiliaryIndicator");
        tag.remove("CantileverSignalCount");

        CFRSignalConfiguration restored = CFRSignalConfiguration.read(tag);

        assertEquals(SignalMount.MAST, restored.mount());
        assertEquals(SignalFinish.CLASSIC, restored.finish());
        assertEquals(AuxiliaryIndicator.NONE, restored.auxiliaryIndicator());
        assertEquals(1, restored.cantileverSignalCount());
    }

    @Test
    void removedGantryMountMigratesToCantilever() {
        CompoundTag tag = CFRSignalConfiguration.defaults().write();
        tag.putInt("Version", 6);
        tag.putString("Mount", "gantry");

        CFRSignalConfiguration restored = CFRSignalConfiguration.read(tag);

        assertEquals(SignalMount.CANTILEVER, restored.mount());
        assertEquals(1, restored.cantileverSignalCount());
    }

    @Test
    void prePositionConfigurationMigratesToNormalPlatePosition() {
        CompoundTag tag = CFRSignalConfiguration.defaults().write();
        tag.putInt("Version", 4);
        tag.remove("IdentificationPlatePosition");

        CFRSignalConfiguration restored = CFRSignalConfiguration.read(tag);

        assertEquals(IdentificationPlatePosition.NORMAL, restored.identificationPlatePosition());
    }

    @Test
    void plateTextIsBoundedAndSanitized() {
        assertEquals("X4DROP", CFRSignalConfiguration.sanitizeIdentificationText(" X4§DROP! "));
        assertEquals("12345678", CFRSignalConfiguration.sanitizeIdentificationText("123456789"));
        assertEquals(false, CFRSignalConfiguration.isValidIdentificationText("too-long-plate"));
        assertEquals(false, CFRSignalConfiguration.isValidIdentificationText("X4!"));
        assertEquals(true, CFRSignalConfiguration.isValidIdentificationText("X4-A"));
    }

    @Test
    void mechanicalSemaphoreFamilyRoundTrips() {
        CFRSignalConfiguration configuration = CFRSignalConfiguration.defaults()
                .withFamily(SignalFamily.MECHANICAL_SEMAPHORE)
                .withHeadAlignment(SignalHeadAlignment.LEFT);

        CFRSignalConfiguration restored = CFRSignalConfiguration.read(configuration.write());

        assertEquals(SignalFamily.MECHANICAL_SEMAPHORE, restored.family());
        assertEquals(SignalHeadAlignment.RIGHT, restored.headAlignment());
        assertEquals(
                CFRSignalConfiguration.MIN_RENDERED_MAST_HEIGHT + 1,
                restored.renderedMastHeight()
        );
        assertEquals(
                CFRSignalConfiguration.MIN_RENDERED_MAST_HEIGHT,
                CFRSignalConfiguration.defaults().renderedMastHeight()
        );
        assertEquals(
                7,
                new CFRSignalConfiguration(
                        CFRSignalConfiguration.CURRENT_VERSION,
                        SignalFamily.THREE_LIGHT,
                        CFRSignalConfiguration.MAX_MAST_HEIGHT
                ).renderedMastHeight()
        );
    }
}
