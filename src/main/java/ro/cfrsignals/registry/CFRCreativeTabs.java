package ro.cfrsignals.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import ro.cfrsignals.CFRSignals;

public final class CFRCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CFRSignals.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register(
            "main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.cfrsignals.main"))
                    .icon(() -> CFRItems.SIGNAL_CONTROLLER.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(CFRItems.SIGNAL_BASE.get());
                        output.accept(CFRItems.SIGNAL_POLE.get());
                        output.accept(CFRItems.SIGNAL_BRIDGE_BEAM.get());
                        output.accept(CFRItems.SIGNAL_CONTROLLER.get());
                    })
                    .build()
    );

    private CFRCreativeTabs() {
    }

    public static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
    }
}
