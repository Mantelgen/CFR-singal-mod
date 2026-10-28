package ro.cfrsignals.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import ro.cfrsignals.config.CFRSignalConfiguration.SignalFamily;

final class SignalOptionCompatibilityTest {
    @Test
    void mechanicalSignalsSupportClassicMastAndBridge() {
        assertEquals(
                java.util.List.of(SignalMount.MAST, SignalMount.CANTILEVER),
                SignalOptionCompatibility.allowedMounts(SignalFamily.MECHANICAL_SEMAPHORE)
        );
        assertEquals(
                java.util.List.of(SignalFinish.CLASSIC),
                SignalOptionCompatibility.allowedFinishes(SignalFamily.MECHANICAL_SEMAPHORE)
        );
        assertEquals(
                java.util.List.of(AuxiliaryIndicator.NONE),
                SignalOptionCompatibility.allowedIndicators(SignalFamily.MECHANICAL_SEMAPHORE)
        );
        assertTrue(SignalOptionCompatibility.supportsElectricalBox(
                SignalFamily.MECHANICAL_SEMAPHORE,
                SignalMount.MAST
        ));
    }

    @Test
    void legacyEntryRemainsOnThePhysicalMastWhileShuntingAlsoSupportsWalls() {
        assertEquals(
                java.util.List.of(SignalMount.MAST),
                SignalOptionCompatibility.allowedMounts(SignalFamily.LEGACY_DWARF_ENTRY)
        );
        assertEquals(
                java.util.List.of(SignalMount.MAST, SignalMount.CANTILEVER, SignalMount.WALL),
                SignalOptionCompatibility.allowedMounts(SignalFamily.SHUNTING)
        );
    }

    @Test
    void wallMountIsAvailableForElectricHeadsWithoutMastAccessories() {
        assertTrue(SignalOptionCompatibility.allowedMounts(SignalFamily.TWO_LIGHT).contains(SignalMount.WALL));
        assertTrue(SignalOptionCompatibility.allowedMounts(SignalFamily.TMV).contains(SignalMount.WALL));
        assertFalse(SignalOptionCompatibility.allowedMounts(SignalFamily.MECHANICAL_SEMAPHORE)
                .contains(SignalMount.WALL));
        assertFalse(SignalOptionCompatibility.supportsLadder(SignalFamily.TWO_LIGHT, SignalMount.WALL));
        assertFalse(SignalOptionCompatibility.supportsElectricalBox(SignalFamily.TWO_LIGHT, SignalMount.WALL));
        assertFalse(SignalOptionCompatibility.supportsHeadAlignment(SignalFamily.TWO_LIGHT, SignalMount.WALL));
    }

    @Test
    void circulationAndTmvSignalsSupportAuxiliaryIndicators() {
        assertTrue(SignalOptionCompatibility.allowedIndicators(SignalFamily.TWO_LIGHT)
                .contains(AuxiliaryIndicator.SHORT_DISTANCE));
        assertTrue(SignalOptionCompatibility.allowedIndicators(SignalFamily.TMV)
                .contains(AuxiliaryIndicator.SPEED_100));
        assertFalse(SignalOptionCompatibility.allowedIndicators(SignalFamily.REPEATER)
                .contains(AuxiliaryIndicator.SPEED_30));
    }

    @Test
    void invalidSavedCombinationNormalizesToFamilyDefaults() {
        CFRSignalConfiguration configuration = new CFRSignalConfiguration(
                CFRSignalConfiguration.CURRENT_VERSION,
                SignalFamily.MECHANICAL_SEMAPHORE,
                CFRSignalConfiguration.DEFAULT_MAST_HEIGHT,
                true,
                true,
                true,
                "M1",
                SignalHeadAlignment.CENTER,
                IdentificationPlatePosition.NORMAL,
                SignalMount.DWARF,
                SignalFinish.MODERN_GRAY,
                AuxiliaryIndicator.SPEED_60,
                5
        );

        assertEquals(SignalMount.MAST, configuration.mount());
        assertEquals(SignalFinish.CLASSIC, configuration.finish());
        assertEquals(AuxiliaryIndicator.NONE, configuration.auxiliaryIndicator());
        assertEquals(SignalHeadAlignment.RIGHT, configuration.headAlignment());
        assertEquals(1, configuration.cantileverSignalCount());
        assertTrue(configuration.electricalBoxEnabled());
    }
}
