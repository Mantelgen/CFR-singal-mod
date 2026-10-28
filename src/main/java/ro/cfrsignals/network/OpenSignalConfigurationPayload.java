package ro.cfrsignals.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import ro.cfrsignals.CFRSignals;
import ro.cfrsignals.config.CFRSignalConfiguration;

public record OpenSignalConfigurationPayload(
        BlockPos position,
        int familyId,
        boolean ladderEnabled,
        boolean electricalBoxEnabled,
        boolean identificationPlateEnabled,
        String identificationText,
        int headAlignmentId,
        int identificationPlatePositionId,
        int mountId,
        int finishId,
        int auxiliaryIndicatorId,
        int cantileverSignalCount
) implements CustomPacketPayload {
    public static final Type<OpenSignalConfigurationPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CFRSignals.MOD_ID, "open_signal_configuration")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenSignalConfigurationPayload> STREAM_CODEC =
            CustomPacketPayload.codec(OpenSignalConfigurationPayload::write, OpenSignalConfigurationPayload::read);

    public OpenSignalConfigurationPayload(BlockPos position, CFRSignalConfiguration configuration) {
        this(
                position,
                configuration.family().ordinal(),
                configuration.ladderEnabled(),
                configuration.electricalBoxEnabled(),
                configuration.identificationPlateEnabled(),
                configuration.identificationText(),
                configuration.headAlignment().ordinal(),
                configuration.identificationPlatePosition().ordinal(),
                configuration.mount().ordinal(),
                configuration.finish().ordinal(),
                configuration.auxiliaryIndicator().ordinal(),
                configuration.cantileverSignalCount()
        );
    }

    private static OpenSignalConfigurationPayload read(RegistryFriendlyByteBuf buffer) {
        return new OpenSignalConfigurationPayload(
                buffer.readBlockPos(),
                buffer.readVarInt(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readUtf(CFRSignalConfiguration.MAX_IDENTIFICATION_TEXT_LENGTH),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt()
        );
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(position);
        buffer.writeVarInt(familyId);
        buffer.writeBoolean(ladderEnabled);
        buffer.writeBoolean(electricalBoxEnabled);
        buffer.writeBoolean(identificationPlateEnabled);
        buffer.writeUtf(identificationText, CFRSignalConfiguration.MAX_IDENTIFICATION_TEXT_LENGTH);
        buffer.writeVarInt(headAlignmentId);
        buffer.writeVarInt(identificationPlatePositionId);
        buffer.writeVarInt(mountId);
        buffer.writeVarInt(finishId);
        buffer.writeVarInt(auxiliaryIndicatorId);
        buffer.writeVarInt(cantileverSignalCount);
    }

    @Override
    public Type<OpenSignalConfigurationPayload> type() {
        return TYPE;
    }
}
