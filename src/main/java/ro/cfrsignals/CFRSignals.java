package ro.cfrsignals;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.slf4j.Logger;
import ro.cfrsignals.registry.CFRBlocks;
import ro.cfrsignals.registry.CFRBlockEntities;
import ro.cfrsignals.registry.CFRCreativeTabs;
import ro.cfrsignals.registry.CFRItems;
import ro.cfrsignals.network.CFRNetwork;
import ro.cfrsignals.signal.SignalDefinitionRegistry;

@Mod(CFRSignals.MOD_ID)
public final class CFRSignals {
    public static final String MOD_ID = "cfrsignals";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CFRSignals(IEventBus modEventBus) {
        CFRBlocks.register(modEventBus);
        CFRBlockEntities.register(modEventBus);
        CFRItems.register(modEventBus);
        CFRCreativeTabs.register(modEventBus);
        modEventBus.addListener(CFRNetwork::registerServerPayloads);
        NeoForge.EVENT_BUS.addListener(CFRSignals::registerReloadListeners);

        LOGGER.info("CFR Signals initialized");
    }

    private static void registerReloadListeners(AddReloadListenerEvent event) {
        event.addListener(SignalDefinitionRegistry.INSTANCE);
    }
}
