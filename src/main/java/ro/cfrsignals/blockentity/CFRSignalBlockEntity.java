package ro.cfrsignals.blockentity;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.content.trains.graph.EdgePointType;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.graph.TrackGraphLocation;
import com.simibubi.create.content.trains.signal.SignalBoundary;
import com.simibubi.create.content.trains.signal.SignalBlockEntity.SignalState;
import com.simibubi.create.content.trains.track.TrackTargetingBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import ro.cfrsignals.config.CFRSignalConfiguration;
import ro.cfrsignals.config.CFRSignalConfiguration.SignalFamily;
import ro.cfrsignals.config.SignalMount;
import ro.cfrsignals.registry.CFRBlockEntities;
import ro.cfrsignals.railway.CFRSignalAspectResolver;
import ro.cfrsignals.railway.CFRSignalAspectResolver.Reason;
import ro.cfrsignals.railway.CFRSignalAspectResolver.Resolution;
import ro.cfrsignals.railway.CFRSignalLinkSnapshot;
import ro.cfrsignals.signal.CFRSignalAspect;
import ro.cfrsignals.signal.CFRSignalAssembly;
import ro.cfrsignals.signal.CFRSemaphoreAnimation;
import ro.cfrsignals.block.CFRSignalBlock;

public class CFRSignalBlockEntity extends SmartBlockEntity {
    private static final String CONFIGURATION_KEY = "Configuration";
    private static final String ASPECT_KEY = "Aspect";
    private static final double MAX_RENDER_RADIUS = 4.0D;

    private CFRSignalConfiguration configuration = CFRSignalConfiguration.defaults();
    private CFRSignalAspect aspect = CFRSignalAspect.RED;
    private SignalState createSignalState = SignalState.INVALID;
    private final CFRSignalAspectResolver aspectResolver = new CFRSignalAspectResolver();
    private Resolution signalResolution = CFRSignalAspectResolver.manual(CFRSignalAspect.RED);
    private TrackTargetingBehaviour<SignalBoundary> trackTargeting;
    private TrackGraph linkedGraph;
    private UUID linkedGraphBoundaryId;
    private float clientPreviousArmAngle;
    private float clientArmAngle;
    private float clientTargetArmAngle;
    private boolean clientArmAnimationInitialized;

    public CFRSignalBlockEntity(BlockPos pos, BlockState state) {
        this(CFRBlockEntities.SIGNAL_CONTROLLER.get(), pos, state);
    }

    public CFRSignalBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        trackTargeting = new TrackTargetingBehaviour<>(this, EdgePointType.SIGNAL);
        behaviours.add(trackTargeting);
    }

    public CFRSignalConfiguration getConfiguration() {
        return configuration;
    }

    public CFRSignalAspect getAspect() {
        return aspect;
    }

    public CFRSignalLinkSnapshot getLinkSnapshot() {
        return CFRSignalLinkSnapshot.from(trackTargeting, worldPosition);
    }

    public SignalState getCreateSignalState() {
        return createSignalState;
    }

    public Resolution getSignalResolution() {
        return signalResolution;
    }

    public CFRSignalAssembly.Result getAssemblyResult() {
        if (level == null || !getBlockState().hasProperty(CFRSignalBlock.FACING)) {
            return new CFRSignalAssembly.Result(CFRSignalAssembly.Status.NO_BASE, 0);
        }
        if (configuration.mount() == SignalMount.WALL) {
            var facing = getBlockState().getValue(CFRSignalBlock.FACING);
            var supportPosition = worldPosition.relative(facing.getOpposite());
            return level.getBlockState(supportPosition).isAir()
                    ? new CFRSignalAssembly.Result(CFRSignalAssembly.Status.NO_WALL, 0)
                    : new CFRSignalAssembly.Result(CFRSignalAssembly.Status.COMPLETE, 0);
        }
        if (configuration.mount() == SignalMount.CANTILEVER) {
            var facing = getBlockState().getValue(CFRSignalBlock.FACING);
            return configuration.family() == SignalFamily.MECHANICAL_SEMAPHORE
                    ? CFRSignalAssembly.inspectMechanicalBridge(level, worldPosition, facing)
                    : CFRSignalAssembly.inspectBridge(level, worldPosition, facing);
        }
        if (configuration.mount() == SignalMount.POLE_INLINE) {
            return CFRSignalAssembly.inspectInlinePole(
                    level,
                    worldPosition,
                    getBlockState().getValue(CFRSignalBlock.FACING)
            );
        }
        return CFRSignalAssembly.inspect(
                level,
                worldPosition,
                getBlockState().getValue(CFRSignalBlock.FACING)
        );
    }

    public boolean hasCompleteAssembly() {
        return getAssemblyResult().complete();
    }

    public boolean isCreateControlled() {
        return trackTargeting != null && trackTargeting.hasValidTrack();
    }

    public boolean hasConfiguredTrackTarget() {
        return trackTargeting != null
                && (!trackTargeting.getGlobalPosition().equals(worldPosition)
                || trackTargeting.getTargetBezier() != null);
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null) {
            return;
        }
        if (level.isClientSide) {
            tickClientArmAnimation();
            return;
        }
        if (!hasCompleteAssembly()) {
            createSignalState = SignalState.INVALID;
            setAspect(CFRSignalAspect.RED);
            clearLinkedGraph();
            return;
        }
        if (!isCreateControlled()) {
            createSignalState = SignalState.INVALID;
            if (hasConfiguredTrackTarget()) {
                signalResolution = CFRSignalAspectResolver.missingTrack();
                setAspect(CFRSignalAspect.RED);
            } else {
                updateManualResolution();
            }
            clearLinkedGraph();
            return;
        }

        SignalBoundary boundary = trackTargeting.getEdgePoint();
        signalResolution = aspectResolver.resolve(
                configuration.family(),
                boundary,
                worldPosition,
                resolveLinkedGraph(boundary),
                Create.RAILWAYS.version,
                level.getGameTime()
        );
        createSignalState = signalResolution.createState();
        setAspect(signalResolution.aspect());
    }

    public void setConfiguration(CFRSignalConfiguration configuration) {
        CFRSignalConfiguration validated = Objects.requireNonNullElseGet(
                configuration,
                CFRSignalConfiguration::defaults
        );
        if (this.configuration.equals(validated)) {
            return;
        }
        this.configuration = validated;
        if (!validated.family().supportsYellow() && aspect == CFRSignalAspect.YELLOW) {
            aspect = CFRSignalAspect.RED;
        }
        if (!isCreateControlled()) {
            updateManualResolution();
        }
        setChangedAndSync();
    }

    public void setAspect(CFRSignalAspect aspect) {
        CFRSignalAspect validated = Objects.requireNonNullElse(aspect, CFRSignalAspect.RED);
        if (this.aspect == validated) {
            return;
        }
        this.aspect = validated;
        setChangedAndSync();
    }

    public CFRSignalAspect cycleDebugAspect() {
        setAspect(aspect.nextDebugAspect(configuration.family().supportsYellow()));
        updateManualResolution();
        return aspect;
    }

    public float getClientSemaphoreArmAngle(float partialTick) {
        return clientPreviousArmAngle
                + (clientArmAngle - clientPreviousArmAngle) * partialTick;
    }

    public float getClientSemaphoreTargetArmAngle() {
        return clientTargetArmAngle;
    }

    public SignalFamily toggleDebugFamily() {
        SignalFamily family = configuration.family() == SignalFamily.TWO_LIGHT
                ? SignalFamily.THREE_LIGHT
                : SignalFamily.TWO_LIGHT;
        configuration = configuration.withFamily(family);
        if (family == SignalFamily.TWO_LIGHT && aspect == CFRSignalAspect.YELLOW) {
            aspect = CFRSignalAspect.RED;
        }
        if (!isCreateControlled()) {
            updateManualResolution();
        }
        setChangedAndSync();
        return family;
    }

    private TrackGraph resolveLinkedGraph(SignalBoundary boundary) {
        if (boundary == null) {
            clearLinkedGraph();
            return null;
        }

        UUID boundaryId = boundary.getId();
        if (linkedGraph != null
                && Objects.equals(linkedGraphBoundaryId, boundaryId)
                && linkedGraph.getPoint(EdgePointType.SIGNAL, boundaryId) == boundary) {
            return linkedGraph;
        }

        TrackGraphLocation location = trackTargeting.determineGraphLocation();
        linkedGraph = location == null ? null : location.graph;
        linkedGraphBoundaryId = linkedGraph == null ? null : boundaryId;
        return linkedGraph;
    }

    private void clearLinkedGraph() {
        linkedGraph = null;
        linkedGraphBoundaryId = null;
        aspectResolver.invalidateTopology();
    }

    private void updateManualResolution() {
        if (signalResolution.reason() != Reason.MANUAL_DEBUG || signalResolution.aspect() != aspect) {
            signalResolution = CFRSignalAspectResolver.manual(aspect);
        }
    }

    private void tickClientArmAnimation() {
        if (configuration.family() != SignalFamily.MECHANICAL_SEMAPHORE) {
            clientArmAnimationInitialized = false;
            clientPreviousArmAngle = CFRSemaphoreAnimation.STOP_ANGLE;
            clientArmAngle = CFRSemaphoreAnimation.STOP_ANGLE;
            clientTargetArmAngle = CFRSemaphoreAnimation.STOP_ANGLE;
            return;
        }

        float nextTarget = CFRSemaphoreAnimation.targetAngle(aspect);
        if (!clientArmAnimationInitialized) {
            clientTargetArmAngle = nextTarget;
            clientPreviousArmAngle = clientTargetArmAngle;
            clientArmAngle = clientTargetArmAngle;
            clientArmAnimationInitialized = true;
            return;
        }

        if (Float.compare(clientTargetArmAngle, nextTarget) != 0) {
            clientTargetArmAngle = nextTarget;
            playClientSemaphoreTransitionSound(nextTarget == CFRSemaphoreAnimation.PROCEED_ANGLE);
        }

        clientPreviousArmAngle = clientArmAngle;
        clientArmAngle = CFRSemaphoreAnimation.approachTarget(clientArmAngle, clientTargetArmAngle);
    }

    private void playClientSemaphoreTransitionSound(boolean proceeding) {
        level.playLocalSound(
                worldPosition,
                proceeding ? SoundEvents.IRON_TRAPDOOR_OPEN : SoundEvents.IRON_TRAPDOOR_CLOSE,
                SoundSource.BLOCKS,
                0.65F,
                proceeding ? 0.85F : 0.75F,
                false
        );
    }

    public boolean toggleDebugLadder() {
        boolean enabled = !configuration.ladderEnabled();
        setConfiguration(configuration.withLadderEnabled(enabled));
        return enabled;
    }

    public boolean toggleDebugElectricalBox() {
        boolean enabled = !configuration.electricalBoxEnabled();
        setConfiguration(configuration.withElectricalBoxEnabled(enabled));
        return enabled;
    }

    public boolean toggleDebugIdentificationPlate() {
        boolean enabled = !configuration.identificationPlateEnabled();
        setConfiguration(configuration.withIdentificationPlateEnabled(enabled));
        return enabled;
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            notifyUpdate();
        }
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put(CONFIGURATION_KEY, configuration.write());
        tag.putString(ASPECT_KEY, aspect.getSerializedName());
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        configuration = tag.contains(CONFIGURATION_KEY)
                ? CFRSignalConfiguration.read(tag.getCompound(CONFIGURATION_KEY))
                : CFRSignalConfiguration.defaults();
        aspect = CFRSignalAspect.fromSerializedName(tag.getString(ASPECT_KEY));
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return new AABB(worldPosition).inflate(MAX_RENDER_RADIUS, 3.0D, MAX_RENDER_RADIUS);
    }
}
