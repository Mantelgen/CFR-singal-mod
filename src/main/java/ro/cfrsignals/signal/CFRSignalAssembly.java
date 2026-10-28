package ro.cfrsignals.signal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import ro.cfrsignals.block.CFRSignalBaseBlock;
import ro.cfrsignals.block.CFRSignalBlock;
import ro.cfrsignals.block.CFRSignalBridgeBeamBlock;
import ro.cfrsignals.block.CFRSignalPoleBlock;
import ro.cfrsignals.blockentity.CFRSignalBlockEntity;
import ro.cfrsignals.config.SignalMount;
import ro.cfrsignals.registry.CFRBlocks;

/** Validates the physical base/pole/head column beneath a signal head. */
public final class CFRSignalAssembly {
    public static final int MIN_POLES = 1;
    public static final int MAX_POLES = 12;
    public static final int MAX_BRIDGE_SPAN = 8;

    private CFRSignalAssembly() {
    }

    public static Result inspect(BlockGetter level, BlockPos headPosition, Direction headFacing) {
        BlockPos.MutableBlockPos cursor = headPosition.below().mutable();
        int poles = 0;

        while (level.getBlockState(cursor).is(CFRBlocks.SIGNAL_POLE.get())) {
            BlockState pole = level.getBlockState(cursor);
            if (pole.getValue(CFRSignalPoleBlock.FACING) != headFacing) {
                return new Result(Status.MISALIGNED, poles);
            }
            poles++;
            if (poles > MAX_POLES) {
                return new Result(Status.TOO_TALL, poles);
            }
            cursor.move(Direction.DOWN);
        }

        if (poles < MIN_POLES) {
            return new Result(Status.NO_POLE, poles);
        }

        BlockState base = level.getBlockState(cursor);
        if (!base.is(CFRBlocks.SIGNAL_BASE.get())) {
            return new Result(Status.NO_BASE, poles);
        }
        if (base.getValue(CFRSignalBaseBlock.FACING) != headFacing) {
            return new Result(Status.MISALIGNED, poles);
        }
        return new Result(Status.COMPLETE, poles);
    }

    public static Result inspectBridge(BlockGetter level, BlockPos headPosition, Direction headFacing) {
        BlockPos beamPosition = headPosition.above();
        return inspectBridgeFromBeam(level, beamPosition, headFacing);
    }

    /** Validates a head that replaces one mast segment while the pole continues above it. */
    public static Result inspectInlinePole(
            BlockGetter level,
            BlockPos headPosition,
            Direction headFacing
    ) {
        BlockState upperPole = level.getBlockState(headPosition.above());
        if (!upperPole.is(CFRBlocks.SIGNAL_POLE.get())) {
            return new Result(Status.NO_UPPER_POLE, 0);
        }
        if (upperPole.getValue(CFRSignalPoleBlock.FACING) != headFacing) {
            return new Result(Status.MISALIGNED, 0);
        }

        BlockPos.MutableBlockPos cursor = headPosition.below().mutable();
        int supportSegments = 0;
        int physicalPoles = 0;
        while (true) {
            BlockState state = level.getBlockState(cursor);
            if (state.is(CFRBlocks.SIGNAL_POLE.get())) {
                if (state.getValue(CFRSignalPoleBlock.FACING) != headFacing) {
                    return new Result(Status.MISALIGNED, physicalPoles);
                }
                physicalPoles++;
            } else if (isInlineHead(level, cursor)) {
                if (state.getValue(CFRSignalBlock.FACING) != headFacing) {
                    return new Result(Status.MISALIGNED, physicalPoles);
                }
            } else {
                break;
            }
            supportSegments++;
            if (supportSegments > MAX_POLES) {
                return new Result(Status.TOO_TALL, physicalPoles);
            }
            cursor.move(Direction.DOWN);
        }

        if (physicalPoles < MIN_POLES) {
            return new Result(Status.NO_POLE, physicalPoles);
        }
        BlockState base = level.getBlockState(cursor);
        if (!base.is(CFRBlocks.SIGNAL_BASE.get())) {
            return new Result(Status.NO_BASE, physicalPoles);
        }
        if (base.getValue(CFRSignalBaseBlock.FACING) != headFacing) {
            return new Result(Status.MISALIGNED, physicalPoles);
        }
        return new Result(Status.COMPLETE, physicalPoles);
    }

    public static boolean isInlineHead(BlockGetter level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        if (!state.is(CFRBlocks.SIGNAL_CONTROLLER.get())) {
            return false;
        }
        return level.getBlockEntity(position) instanceof CFRSignalBlockEntity signal
                && signal.getConfiguration().mount() == SignalMount.POLE_INLINE;
    }

    /** Chooses the alternating color for the mast segment rendered through an inline head. */
    public static boolean inlineSegmentIsWhite(BlockGetter level, BlockPos headPosition) {
        BlockState below = level.getBlockState(headPosition.below());
        if (below.is(CFRBlocks.SIGNAL_POLE.get())) {
            return !below.getValue(CFRSignalPoleBlock.WHITE);
        }
        return below.is(CFRBlocks.SIGNAL_BASE.get());
    }

    /** Mechanical bridge signals stand above the crossbeam instead of hanging below it. */
    public static Result inspectMechanicalBridge(
            BlockGetter level,
            BlockPos headPosition,
            Direction headFacing
    ) {
        BlockPos beamPosition = headPosition.below();
        return inspectBridgeFromBeam(level, beamPosition, headFacing);
    }

    private static Result inspectBridgeFromBeam(
            BlockGetter level,
            BlockPos beamPosition,
            Direction headFacing
    ) {
        BlockState beam = level.getBlockState(beamPosition);
        if (!beam.is(CFRBlocks.SIGNAL_BRIDGE_BEAM.get())) {
            return new Result(Status.NO_BRIDGE, 0);
        }
        if (beam.getValue(CFRSignalBridgeBeamBlock.FACING) != headFacing) {
            return new Result(Status.MISALIGNED, 0);
        }

        Result directSupport = inspectBridgeSupport(level, beamPosition, headFacing);
        if (directSupport != null) {
            return directSupport;
        }

        Direction first = headFacing.getClockWise();
        Direction second = first.getOpposite();
        for (Direction direction : new Direction[]{first, second}) {
            BlockPos.MutableBlockPos cursor = beamPosition.mutable();
            for (int distance = 1; distance <= MAX_BRIDGE_SPAN; distance++) {
                cursor.move(direction);
                BlockState section = level.getBlockState(cursor);
                if (!section.is(CFRBlocks.SIGNAL_BRIDGE_BEAM.get())) {
                    break;
                }
                if (section.getValue(CFRSignalBridgeBeamBlock.FACING) != headFacing) {
                    return new Result(Status.MISALIGNED, 0);
                }
                Result support = inspectBridgeSupport(level, cursor, headFacing);
                if (support != null) {
                    return support;
                }
            }
        }
        return new Result(Status.NO_BRIDGE_SUPPORT, 0);
    }

    private static Result inspectBridgeSupport(
            BlockGetter level,
            BlockPos beamPosition,
            Direction headFacing
    ) {
        if (!level.getBlockState(beamPosition.below()).is(CFRBlocks.SIGNAL_POLE.get())) {
            return null;
        }
        return inspect(level, beamPosition, headFacing);
    }

    public enum Status {
        COMPLETE,
        NO_POLE,
        NO_BASE,
        NO_WALL,
        NO_UPPER_POLE,
        NO_BRIDGE,
        NO_BRIDGE_SUPPORT,
        MISALIGNED,
        TOO_TALL
    }

    public record Result(Status status, int poleCount) {
        public boolean complete() {
            return status == Status.COMPLETE;
        }

        public String translationKey() {
            return "assembly.cfrsignals." + status.name().toLowerCase(java.util.Locale.ROOT);
        }
    }
}
