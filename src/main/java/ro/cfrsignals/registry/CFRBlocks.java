package ro.cfrsignals.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import ro.cfrsignals.CFRSignals;
import ro.cfrsignals.block.CFRSignalBaseBlock;
import ro.cfrsignals.block.CFRSignalBlock;
import ro.cfrsignals.block.CFRSignalBridgeBeamBlock;
import ro.cfrsignals.block.CFRSignalPoleBlock;

public final class CFRBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CFRSignals.MOD_ID);

    public static final DeferredBlock<Block> TEST_BLOCK = BLOCKS.registerSimpleBlock(
            "test_block",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.METAL)
    );

    public static final DeferredBlock<CFRSignalBlock> SIGNAL_CONTROLLER = BLOCKS.register(
            "signal_controller",
            () -> new CFRSignalBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion())
    );

    public static final DeferredBlock<CFRSignalBaseBlock> SIGNAL_BASE = BLOCKS.register(
            "signal_base",
            () -> new CFRSignalBaseBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.0F, 8.0F)
                    .sound(SoundType.STONE)
                    .noOcclusion())
    );

    public static final DeferredBlock<CFRSignalPoleBlock> SIGNAL_POLE = BLOCKS.register(
            "signal_pole",
            () -> new CFRSignalPoleBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.5F, 7.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion())
    );

    public static final DeferredBlock<CFRSignalBridgeBeamBlock> SIGNAL_BRIDGE_BEAM = BLOCKS.register(
            "signal_bridge_beam",
            () -> new CFRSignalBridgeBeamBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.0F, 8.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion())
    );

    private CFRBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
