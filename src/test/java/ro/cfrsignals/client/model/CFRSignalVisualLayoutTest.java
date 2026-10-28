package ro.cfrsignals.client.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import ro.cfrsignals.config.CFRSignalConfiguration.SignalFamily;
import ro.cfrsignals.config.IdentificationPlatePosition;
import ro.cfrsignals.config.SignalHeadAlignment;
import ro.cfrsignals.config.SignalMount;

final class CFRSignalVisualLayoutTest {
    @Test
    void modernHeadUsesRegularBlockScaleAndStaysInFrontOfMast() {
        assertEquals(1.0F, CFRSignalVisualLayout.modernHeadScale());
        assertTrue(CFRSignalVisualLayout.headForwardOffsetZ() < 0.0D);
    }

    @Test
    void mechanicalSemaphoreIsEnlarged() {
        assertEquals(1.15F, CFRSignalVisualLayout.mechanicalSemaphoreScale());
    }

    @Test
    void inlineMechanicalSemaphoreProjectsInFrontOfContinuousPole() {
        assertEquals(-6.0D / 16.0D, CFRSignalVisualLayout.mechanicalInlineForwardOffsetZ());
        assertTrue(
                CFRSignalVisualLayout.mechanicalInlineForwardOffsetZ()
                        < CFRSignalVisualLayout.headForwardOffsetZ()
        );
    }

    @Test
    void identificationTextShrinksOnlyWhenNeeded() {
        assertEquals(1.6F, CFRSignalVisualLayout.identificationPlateScale());
        assertEquals(1.2F, CFRSignalVisualLayout.bridgeIdentificationPlateScale());
        assertTrue(CFRSignalVisualLayout.identificationTextOffsetZ() < 5.62D / 16.0D);
        assertEquals(180.0F, CFRSignalVisualLayout.identificationTextYRotation());
        assertEquals(0.0125F, CFRSignalVisualLayout.identificationTextScale(20));
        assertEquals(0.006875F, CFRSignalVisualLayout.identificationTextScale(48), 0.000001F);
    }

    @Test
    void identificationTextIsSkippedBeyondReadableDistance() {
        assertTrue(CFRSignalVisualLayout.shouldRenderIdentificationText(48.0D * 48.0D));
        assertEquals(false, CFRSignalVisualLayout.shouldRenderIdentificationText(48.01D * 48.01D));
    }

    @Test
    void platePositionsAreOrderedAndFamilyAware() {
        double modernNormal = CFRSignalVisualLayout.identificationPlateOffsetY(
                SignalFamily.TWO_LIGHT,
                5,
                IdentificationPlatePosition.NORMAL
        );
        assertTrue(CFRSignalVisualLayout.identificationPlateOffsetY(
                SignalFamily.TWO_LIGHT,
                5,
                IdentificationPlatePosition.HIGH
        ) > modernNormal);
        assertTrue(CFRSignalVisualLayout.identificationPlateOffsetY(
                SignalFamily.TWO_LIGHT,
                5,
                IdentificationPlatePosition.LOW
        ) < modernNormal);
        assertEquals(
                4.05D,
                CFRSignalVisualLayout.identificationPlateOffsetY(
                        SignalFamily.MECHANICAL_SEMAPHORE,
                        6,
                        IdentificationPlatePosition.NORMAL
                )
        );

        double wallNormal = CFRSignalVisualLayout.identificationPlateOffsetFromHead(
                SignalMount.WALL,
                IdentificationPlatePosition.NORMAL
        );
        assertTrue(CFRSignalVisualLayout.identificationPlateOffsetFromHead(
                SignalMount.WALL,
                IdentificationPlatePosition.HIGH
        ) > wallNormal);
        assertTrue(CFRSignalVisualLayout.identificationPlateOffsetFromHead(
                SignalMount.WALL,
                IdentificationPlatePosition.LOW
        ) < wallNormal);

        double bridgeNormal = CFRSignalVisualLayout.identificationPlateOffsetFromHead(
                SignalMount.CANTILEVER,
                IdentificationPlatePosition.NORMAL
        );
        assertEquals(-0.25D, bridgeNormal);
        assertTrue(CFRSignalVisualLayout.identificationPlateOffsetFromHead(
                SignalMount.CANTILEVER,
                IdentificationPlatePosition.HIGH
        ) > bridgeNormal);
        assertTrue(CFRSignalVisualLayout.identificationPlateOffsetFromHead(
                SignalMount.CANTILEVER,
                IdentificationPlatePosition.LOW
        ) < bridgeNormal);
        assertEquals(
                bridgeNormal,
                CFRSignalVisualLayout.identificationPlateOffsetFromHead(
                        SignalMount.POLE_INLINE,
                        IdentificationPlatePosition.NORMAL
                )
        );
    }

    @Test
    void lateralAlignmentRemainsSymmetric() {
        assertEquals(
                -CFRSignalVisualLayout.headOffsetX(SignalHeadAlignment.LEFT),
                CFRSignalVisualLayout.headOffsetX(SignalHeadAlignment.RIGHT)
        );
        assertEquals(0.0D, CFRSignalVisualLayout.headOffsetX(SignalHeadAlignment.CENTER));
    }

    @Test
    void dwarfHeadIsLoweredOntoItsBase() {
        double dwarfBaseTop = 5.0D / 16.0D;
        double shuntingScale = 0.82D * 0.72D;
        double scaledHeadBottom = CFRSignalVisualLayout.dwarfHeadOffsetY()
                + 0.5D / 16.0D * shuntingScale;

        assertTrue(scaledHeadBottom <= dwarfBaseTop);
    }
}
