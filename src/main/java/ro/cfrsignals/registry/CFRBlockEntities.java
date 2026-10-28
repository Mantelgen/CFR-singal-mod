package ro.cfrsignals.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import ro.cfrsignals.CFRSignals;
import ro.cfrsignals.blockentity.CFRSignalBlockEntity;

public final class CFRBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CFRSignals.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CFRSignalBlockEntity>> SIGNAL_CONTROLLER =
            BLOCK_ENTITIES.register(
                    "signal_controller",
                    () -> BlockEntityType.Builder.of(
                            CFRSignalBlockEntity::new,
                            CFRBlocks.SIGNAL_CONTROLLER.get()
                    ).build(null)
            );

    private CFRBlockEntities() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}
