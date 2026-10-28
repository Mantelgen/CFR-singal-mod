package ro.cfrsignals.signal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class CFRSemaphoreAnimationTest {
    @Test
    void aspectDeterminesTargetAngle() {
        assertEquals(CFRSemaphoreAnimation.PROCEED_ANGLE, CFRSemaphoreAnimation.targetAngle(CFRSignalAspect.GREEN));
        assertEquals(CFRSemaphoreAnimation.STOP_ANGLE, CFRSemaphoreAnimation.targetAngle(CFRSignalAspect.RED));
        assertEquals(CFRSemaphoreAnimation.STOP_ANGLE, CFRSemaphoreAnimation.targetAngle(CFRSignalAspect.YELLOW));
        assertEquals(CFRSemaphoreAnimation.STOP_ANGLE, CFRSemaphoreAnimation.targetAngle(CFRSignalAspect.OFF));
    }

    @Test
    void armApproachesWithoutOvershooting() {
        float angle = CFRSemaphoreAnimation.STOP_ANGLE;
        for (int tick = 0; tick < 18; tick++) {
            angle = CFRSemaphoreAnimation.approachTarget(angle, CFRSemaphoreAnimation.PROCEED_ANGLE);
        }
        assertEquals(CFRSemaphoreAnimation.PROCEED_ANGLE, angle);

        angle = CFRSemaphoreAnimation.approachTarget(angle, CFRSemaphoreAnimation.STOP_ANGLE);
        assertEquals(42.5F, angle);
    }

    @Test
    void linkageMovesOppositeTheArmAtReducedTravel() {
        assertEquals(0.0F, CFRSemaphoreAnimation.linkageAngle(CFRSemaphoreAnimation.STOP_ANGLE));
        assertEquals(
                -15.75F,
                CFRSemaphoreAnimation.linkageAngle(CFRSemaphoreAnimation.PROCEED_ANGLE),
                0.0001F
        );
    }
}
