package ro.cfrsignals.registry;

import com.simibubi.create.content.trains.track.TrackTargetingBlockItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import ro.cfrsignals.CFRSignals;
import ro.cfrsignals.item.CFRSignalControllerItem;
import ro.cfrsignals.item.CFRSignalWrenchItem;

public final class CFRItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CFRSignals.MOD_ID);

    public static final DeferredItem<BlockItem> TEST_BLOCK = ITEMS.registerSimpleBlockItem(
            "test_block",
            CFRBlocks.TEST_BLOCK
    );

    public static final DeferredItem<TrackTargetingBlockItem> SIGNAL_CONTROLLER = ITEMS.register(
            "signal_controller",
            () -> new CFRSignalControllerItem(
                    CFRBlocks.SIGNAL_CONTROLLER.get(),
                    new Item.Properties()
            )
    );

    public static final DeferredItem<BlockItem> SIGNAL_BASE = ITEMS.registerSimpleBlockItem(
            "signal_base",
            CFRBlocks.SIGNAL_BASE
    );

    public static final DeferredItem<BlockItem> SIGNAL_POLE = ITEMS.registerSimpleBlockItem(
            "signal_pole",
            CFRBlocks.SIGNAL_POLE
    );

    public static final DeferredItem<BlockItem> SIGNAL_BRIDGE_BEAM = ITEMS.registerSimpleBlockItem(
            "signal_bridge_beam",
            CFRBlocks.SIGNAL_BRIDGE_BEAM
    );

    public static final DeferredItem<Item> SIGNAL_WRENCH = ITEMS.register(
            "signal_wrench",
            () -> new CFRSignalWrenchItem(new Item.Properties().stacksTo(1))
    );

    private CFRItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
