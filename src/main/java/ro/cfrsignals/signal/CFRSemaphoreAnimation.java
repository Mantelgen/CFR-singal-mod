package ro.cfrsignals.signal;

import net.minecraft.util.Mth;

public final class CFRSemaphoreAnimation {
    public static final float STOP_ANGLE = 0.0F;
    public static final float PROCEED_ANGLE = 45.0F;
    public static final float DEGREES_PER_TICK = 2.5F;
    public static final float LINKAGE_ANGLE_RATIO = -0.35F;

    private CFRSemaphoreAnimation() {
    }

    public static float targetAngle(CFRSignalAspect aspect) {
        return aspect == CFRSignalAspect.GREEN ? PROCEED_ANGLE : STOP_ANGLE;
    }

    public static float approachTarget(float currentAngle, float targetAngle) {
        return Mth.approach(currentAngle, targetAngle, DEGREES_PER_TICK);
    }

    public static float linkageAngle(float armAngle) {
        return armAngle == 0.0F ? 0.0F : armAngle * LINKAGE_ANGLE_RATIO;
    }
}
