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
import ro.cfrsignals.signal.CFRSignalAssembly;

/** A stackable mast section. Each section includes its ladder in the block model. */
public final class CFRSignalPoleBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WHITE = BooleanProperty.create("white");

    public CFRSignalPoleBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(WHITE, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState below = context.getLevel().getBlockState(context.getClickedPos().below());
        Direction facing = context.getHorizontalDirection().getOpposite();
        boolean white = false;
        if (below.is(CFRBlocks.SIGNAL_POLE.get())) {
            facing = below.getValue(FACING);
            white = !below.getValue(WHITE);
        } else if (below.is(CFRBlocks.SIGNAL_BASE.get())) {
            facing = below.getValue(CFRSignalBaseBlock.FACING);
            // The base contains the first red mast section, so the next section is white.
            white = true;
        } else if (below.is(CFRBlocks.SIGNAL_CONTROLLER.get())) {
            facing = below.getValue(CFRSignalBlock.FACING);
            white = !CFRSignalAssembly.inlineSegmentIsWhite(
                    context.getLevel(), context.getClickedPos().below()
            );
        }
        return defaultBlockState().setValue(FACING, facing).setValue(WHITE, white);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WHITE);
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
