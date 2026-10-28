package ro.cfrsignals.network;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import ro.cfrsignals.blockentity.CFRSignalBlockEntity;
import ro.cfrsignals.config.CFRSignalConfiguration.SignalFamily;
import ro.cfrsignals.config.AuxiliaryIndicator;
import ro.cfrsignals.config.IdentificationPlatePosition;
import ro.cfrsignals.config.SignalFinish;
import ro.cfrsignals.config.SignalHeadAlignment;
import ro.cfrsignals.config.SignalMount;
import ro.cfrsignals.registry.CFRItems;

public final class CFRNetwork {
    private static final double MAX_CONFIGURATION_DISTANCE_SQUARED = 64.0D;

    private CFRNetwork() {
    }

    public static void registerServerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("6")
                .playToServer(
                        ApplySignalConfigurationPayload.TYPE,
                        ApplySignalConfigurationPayload.STREAM_CODEC,
                        CFRNetwork::handleApplyConfiguration
                );
    }

    public static void openConfiguration(ServerPlayer player, CFRSignalBlockEntity blockEntity) {
        PacketDistributor.sendToPlayer(
                player,
                new OpenSignalConfigurationPayload(blockEntity.getBlockPos(), blockEntity.getConfiguration())
        );
    }

    private static void handleApplyConfiguration(
            ApplySignalConfigurationPayload payload,
            net.neoforged.neoforge.network.handling.IPayloadContext context
    ) {
        context.enqueueWork(() -> applyConfiguration(payload, context.player()));
    }

    static boolean applyConfiguration(ApplySignalConfigurationPayload payload, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)
                || !payload.hasValidValues()
                || !serverPlayer.mayBuild()
                || !serverPlayer.level().mayInteract(serverPlayer, payload.position())
                || (!CFRItems.isSignalConfigurationTool(serverPlayer.getMainHandItem())
                && !CFRItems.isSignalConfigurationTool(serverPlayer.getOffhandItem()))
                || serverPlayer.distanceToSqr(
                        payload.position().getX() + 0.5D,
                        payload.position().getY() + 0.5D,
                        payload.position().getZ() + 0.5D
                ) > MAX_CONFIGURATION_DISTANCE_SQUARED
                || !serverPlayer.level().isLoaded(payload.position())
                || !(serverPlayer.level().getBlockEntity(payload.position()) instanceof CFRSignalBlockEntity blockEntity)) {
            return false;
        }

        var current = blockEntity.getConfiguration();
        blockEntity.setConfiguration(current.withEditorSettings(
                SignalFamily.fromNetworkId(payload.familyId()),
                payload.ladderEnabled(),
                payload.electricalBoxEnabled(),
                payload.identificationPlateEnabled(),
                payload.identificationText(),
                SignalHeadAlignment.fromNetworkId(payload.headAlignmentId()),
                IdentificationPlatePosition.fromNetworkId(payload.identificationPlatePositionId()),
                SignalMount.fromNetworkId(payload.mountId()),
                SignalFinish.fromNetworkId(payload.finishId()),
                AuxiliaryIndicator.fromNetworkId(payload.auxiliaryIndicatorId()),
                payload.cantileverSignalCount()
        ));
        serverPlayer.displayClientMessage(Component.translatable("message.cfrsignals.configuration_saved"), true);
        return true;
    }
}
