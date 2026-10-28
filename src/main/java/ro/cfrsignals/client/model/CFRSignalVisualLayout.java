package ro.cfrsignals.client.model;

import ro.cfrsignals.config.CFRSignalConfiguration.SignalFamily;
import ro.cfrsignals.config.IdentificationPlatePosition;
import ro.cfrsignals.config.SignalHeadAlignment;
import ro.cfrsignals.config.SignalMount;

public final class CFRSignalVisualLayout {
    // 3.5 model units: a side head clears the slender mast without looking detached.
    private static final double SIDE_HEAD_OFFSET_X = 3.5D / 16.0D;
    // Pull the housing in front of the mast; only its rear mounting spine intersects the pole.
    private static final double HEAD_FORWARD_OFFSET_Z = -3.5D / 16.0D;
    // Inline semaphore housings are much deeper than electric heads. Move the complete
    // mechanism six model units forward, leaving only its rear edge on the pole.
    private static final double MECHANICAL_INLINE_FORWARD_OFFSET_Z = -6.0D / 16.0D;
    private static final double DWARF_HEAD_OFFSET_Y = 0.29D;
    private static final float MODERN_HEAD_SCALE = 1.0F;
    private static final float MECHANICAL_SEMAPHORE_SCALE = 1.15F;
    private static final float IDENTIFICATION_PLATE_SCALE = 1.6F;
    private static final float BRIDGE_IDENTIFICATION_PLATE_SCALE = 1.2F;
    private static final float IDENTIFICATION_TEXT_BASE_SCALE = 0.0125F;
    private static final float IDENTIFICATION_TEXT_MAX_WIDTH = 0.33F;
    private static final double IDENTIFICATION_TEXT_OFFSET_Z = 0.32D;
    private static final float IDENTIFICATION_TEXT_Y_ROTATION = 180.0F;
    private static final double IDENTIFICATION_TEXT_RENDER_DISTANCE = 48.0D;

    private CFRSignalVisualLayout() {
    }

    public static double headOffsetX(SignalHeadAlignment alignment) {
        return switch (alignment) {
            case LEFT -> -SIDE_HEAD_OFFSET_X;
            case CENTER -> 0.0D;
            case RIGHT -> SIDE_HEAD_OFFSET_X;
        };
    }

    public static double headForwardOffsetZ() {
        return HEAD_FORWARD_OFFSET_Z;
    }

    public static double mechanicalInlineForwardOffsetZ() {
        return MECHANICAL_INLINE_FORWARD_OFFSET_Z;
    }

    public static double dwarfHeadOffsetY() {
        return DWARF_HEAD_OFFSET_Y;
    }

    public static float modernHeadScale() {
        return MODERN_HEAD_SCALE;
    }

    public static float mechanicalSemaphoreScale() {
        return MECHANICAL_SEMAPHORE_SCALE;
    }

    public static float identificationPlateScale() {
        return IDENTIFICATION_PLATE_SCALE;
    }

    public static float bridgeIdentificationPlateScale() {
        return BRIDGE_IDENTIFICATION_PLATE_SCALE;
    }

    public static float identificationTextScale(int pixelWidth) {
        if (pixelWidth <= 0) {
            return IDENTIFICATION_TEXT_BASE_SCALE;
        }
        return Math.min(
                IDENTIFICATION_TEXT_BASE_SCALE,
                IDENTIFICATION_TEXT_MAX_WIDTH / pixelWidth
        );
    }

    public static boolean shouldRenderIdentificationText(double squaredDistance) {
        return squaredDistance <= IDENTIFICATION_TEXT_RENDER_DISTANCE * IDENTIFICATION_TEXT_RENDER_DISTANCE;
    }

    public static double identificationTextOffsetZ() {
        return IDENTIFICATION_TEXT_OFFSET_Z;
    }

    public static float identificationTextYRotation() {
        return IDENTIFICATION_TEXT_Y_ROTATION;
    }

    public static double identificationPlateOffsetY(
            SignalFamily family,
            int renderedMastHeight,
            IdentificationPlatePosition position
    ) {
        double normalOffset = family == SignalFamily.MECHANICAL_SEMAPHORE ? 1.95D : 0.95D;
        double positionOffset = switch (position) {
            case HIGH -> family == SignalFamily.MECHANICAL_SEMAPHORE ? 0.6D : 0.15D;
            case NORMAL -> 0.0D;
            case LOW -> -0.6D;
        };
        return renderedMastHeight - normalOffset + positionOffset;
    }

    /** Plate position relative to the physical signal-head block. */
    public static double identificationPlateOffsetFromHead(
            SignalMount mount,
            IdentificationPlatePosition position
    ) {
        double normalOffset = switch (mount) {
            case WALL -> -0.35D;
            case CANTILEVER, POLE_INLINE -> -0.25D;
            default -> -0.55D;
        };
        double positionOffset = switch (position) {
            case HIGH -> mount == SignalMount.CANTILEVER || mount == SignalMount.POLE_INLINE
                    ? 0.10D
                    : 0.30D;
            case NORMAL -> 0.0D;
            case LOW -> mount == SignalMount.CANTILEVER || mount == SignalMount.POLE_INLINE
                    ? -0.10D
                    : -0.30D;
        };
        return normalOffset + positionOffset;
    }

    public static boolean needsSideBracket(SignalHeadAlignment alignment) {
        return alignment != SignalHeadAlignment.CENTER;
    }

    public static boolean mirrorSideBracket(SignalHeadAlignment alignment) {
        return alignment == SignalHeadAlignment.LEFT;
    }
}
