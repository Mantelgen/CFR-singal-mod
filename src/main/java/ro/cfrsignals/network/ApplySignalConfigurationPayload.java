package ro.cfrsignals.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import ro.cfrsignals.CFRSignals;
import ro.cfrsignals.config.CFRSignalConfiguration;
import ro.cfrsignals.config.CFRSignalConfiguration.SignalFamily;
import ro.cfrsignals.config.AuxiliaryIndicator;
import ro.cfrsignals.config.IdentificationPlatePosition;
import ro.cfrsignals.config.SignalFinish;
import ro.cfrsignals.config.SignalHeadAlignment;
import ro.cfrsignals.config.SignalMount;
import ro.cfrsignals.config.SignalOptionCompatibility;

public record ApplySignalConfigurationPayload(
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
    public static final Type<ApplySignalConfigurationPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CFRSignals.MOD_ID, "apply_signal_configuration")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ApplySignalConfigurationPayload> STREAM_CODEC =
            CustomPacketPayload.codec(ApplySignalConfigurationPayload::write, ApplySignalConfigurationPayload::read);

    public ApplySignalConfigurationPayload(
            BlockPos position,
            SignalFamily family,
            boolean ladderEnabled,
            boolean electricalBoxEnabled,
            boolean identificationPlateEnabled,
            String identificationText,
            SignalHeadAlignment headAlignment,
            IdentificationPlatePosition identificationPlatePosition,
            SignalMount mount,
            SignalFinish finish,
            AuxiliaryIndicator auxiliaryIndicator
    ) {
        this(
                position, family, ladderEnabled, electricalBoxEnabled,
                identificationPlateEnabled, identificationText, headAlignment,
                identificationPlatePosition, mount, finish, auxiliaryIndicator,
                CFRSignalConfiguration.DEFAULT_CANTILEVER_SIGNAL_COUNT
        );
    }

    public ApplySignalConfigurationPayload(
            BlockPos position,
            SignalFamily family,
            boolean ladderEnabled,
            boolean electricalBoxEnabled,
            boolean identificationPlateEnabled,
            String identificationText,
            SignalHeadAlignment headAlignment,
            IdentificationPlatePosition identificationPlatePosition,
            SignalMount mount,
            SignalFinish finish,
            AuxiliaryIndicator auxiliaryIndicator,
            int cantileverSignalCount
    ) {
        this(
                position,
                family.ordinal(),
                ladderEnabled,
                electricalBoxEnabled,
                identificationPlateEnabled,
                identificationText,
                headAlignment.ordinal(),
                identificationPlatePosition.ordinal(),
                mount.ordinal(),
                finish.ordinal(),
                auxiliaryIndicator.ordinal(),
                cantileverSignalCount
        );
    }

    public ApplySignalConfigurationPayload(
            BlockPos position,
            SignalFamily family,
            boolean ladderEnabled,
            boolean electricalBoxEnabled,
            boolean identificationPlateEnabled,
            String identificationText,
            SignalHeadAlignment headAlignment,
            IdentificationPlatePosition identificationPlatePosition
    ) {
        this(
                position,
                family,
                ladderEnabled,
                electricalBoxEnabled,
                identificationPlateEnabled,
                identificationText,
                headAlignment,
                identificationPlatePosition,
                SignalMount.MAST,
                SignalFinish.CLASSIC,
                AuxiliaryIndicator.NONE,
                CFRSignalConfiguration.DEFAULT_CANTILEVER_SIGNAL_COUNT
        );
    }

    public ApplySignalConfigurationPayload(
            BlockPos position,
            int familyId,
            boolean ladderEnabled,
            boolean electricalBoxEnabled,
            boolean identificationPlateEnabled,
            String identificationText,
            int headAlignmentId,
            int identificationPlatePositionId
    ) {
        this(position, familyId, ladderEnabled, electricalBoxEnabled, identificationPlateEnabled,
                identificationText, headAlignmentId, identificationPlatePositionId,
                SignalMount.MAST.ordinal(), SignalFinish.CLASSIC.ordinal(), AuxiliaryIndicator.NONE.ordinal(),
                CFRSignalConfiguration.DEFAULT_CANTILEVER_SIGNAL_COUNT);
    }

    public boolean hasValidValues() {
        return SignalFamily.isValidNetworkId(familyId)
                && SignalHeadAlignment.isValidNetworkId(headAlignmentId)
                && IdentificationPlatePosition.isValidNetworkId(identificationPlatePositionId)
                && SignalMount.isValidNetworkId(mountId)
                && SignalFinish.isValidNetworkId(finishId)
                && AuxiliaryIndicator.isValidNetworkId(auxiliaryIndicatorId)
                && cantileverSignalCount >= CFRSignalConfiguration.MIN_CANTILEVER_SIGNAL_COUNT
                && cantileverSignalCount <= CFRSignalConfiguration.MAX_CANTILEVER_SIGNAL_COUNT
                && CFRSignalConfiguration.isValidIdentificationText(identificationText)
                && hasCompatibleOptions();
    }

    private boolean hasCompatibleOptions() {
        SignalFamily family = SignalFamily.fromNetworkId(familyId);
        SignalMount mount = SignalMount.fromNetworkId(mountId);
        SignalFinish finish = SignalFinish.fromNetworkId(finishId);
        AuxiliaryIndicator indicator = AuxiliaryIndicator.fromNetworkId(auxiliaryIndicatorId);
        SignalHeadAlignment alignment = SignalHeadAlignment.fromNetworkId(headAlignmentId);
        return SignalOptionCompatibility.allowedMounts(family).contains(mount)
                && SignalOptionCompatibility.allowedFinishes(family).contains(finish)
                && SignalOptionCompatibility.allowedIndicators(family).contains(indicator)
                && (!ladderEnabled || SignalOptionCompatibility.supportsLadder(family, mount))
                && (!electricalBoxEnabled || SignalOptionCompatibility.supportsElectricalBox(family, mount))
                && (!identificationPlateEnabled
                || SignalOptionCompatibility.supportsIdentificationPlate(family))
                && cantileverSignalCount == CFRSignalConfiguration.DEFAULT_CANTILEVER_SIGNAL_COUNT
                && (SignalOptionCompatibility.supportsHeadAlignment(family, mount)
                || alignment == CFRSignalConfiguration.defaultHeadAlignment(family));
    }

    private static ApplySignalConfigurationPayload read(RegistryFriendlyByteBuf buffer) {
        return new ApplySignalConfigurationPayload(
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
    public Type<ApplySignalConfigurationPayload> type() {
        return TYPE;
    }
}
