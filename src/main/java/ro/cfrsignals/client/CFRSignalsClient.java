package ro.cfrsignals.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import ro.cfrsignals.CFRSignals;
import ro.cfrsignals.client.model.CFRSignalModels;
import ro.cfrsignals.client.renderer.CFRSignalRenderer;
import ro.cfrsignals.registry.CFRBlockEntities;
import ro.cfrsignals.client.screen.CFRSignalConfigurationScreen;
import ro.cfrsignals.network.OpenSignalConfigurationPayload;

@Mod(value = CFRSignals.MOD_ID, dist = Dist.CLIENT)
public final class CFRSignalsClient {
    public CFRSignalsClient(IEventBus modEventBus) {
        modEventBus.addListener(this::registerRenderers);
        modEventBus.addListener(this::registerAdditionalModels);
        modEventBus.addListener(this::registerClientPayloads);
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                CFRBlockEntities.SIGNAL_CONTROLLER.get(),
                CFRSignalRenderer::new
        );
    }

    private void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        CFRSignalModels.registerAdditional(event);
    }

    private void registerClientPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("6")
                .playToClient(
                        OpenSignalConfigurationPayload.TYPE,
                        OpenSignalConfigurationPayload.STREAM_CODEC,
                        (payload, context) -> context.enqueueWork(
                                () -> CFRSignalConfigurationScreen.open(payload)
                        )
                );
    }
}
