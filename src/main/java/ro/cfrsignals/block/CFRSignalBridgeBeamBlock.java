package ro.cfrsignals.block;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import ro.cfrsignals.registry.CFRBlocks;

/** A horizontal bridge segment. Segments can form one-sided L or two-sided T supports. */
public final class CFRSignalBridgeBeamBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty POST = BooleanProperty.create("post");

    public CFRSignalBridgeBeamBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(POST, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var level = context.getLevel();
        var position = context.getClickedPos();
        BlockState below = level.getBlockState(position.below());
        Direction facing = context.getHorizontalDirection().getOpposite();

        if (below.is(CFRBlocks.SIGNAL_POLE.get())) {
            facing = below.getValue(CFRSignalPoleBlock.FACING);
        } else {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockState neighbour = level.getBlockState(position.relative(direction));
                if (neighbour.is(CFRBlocks.SIGNAL_BRIDGE_BEAM.get())) {
                    facing = neighbour.getValue(FACING);
                    break;
                }
            }
        }

        return defaultBlockState()
                .setValue(FACING, facing)
                .setValue(POST, below.is(CFRBlocks.SIGNAL_POLE.get()));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POST);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }
}
