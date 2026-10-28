package ro.cfrsignals.railway;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.simibubi.create.content.trains.graph.EdgePointType;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.signal.SignalBlockEntity.SignalState;
import com.simibubi.create.content.trains.signal.SignalBoundary;
import com.simibubi.create.content.trains.signal.SignalPropagator;
import net.minecraft.core.BlockPos;
import ro.cfrsignals.config.CFRSignalConfiguration.SignalFamily;
import ro.cfrsignals.signal.CFRSignalAspect;

public final class CFRSignalAspectResolver {
    private static final long TOPOLOGY_REFRESH_INTERVAL = 100L;

    private TrackGraph cachedGraph;
    private UUID cachedBoundaryId;
    private Boolean cachedSide;
    private int cachedTopologyVersion = Integer.MIN_VALUE;
    private Map<UUID, Boolean> cachedFollowingSignals = Map.of();
    private long nextTopologyRefresh = Long.MIN_VALUE;
    private Resolution lastResolution;

    public Resolution resolve(
            SignalFamily family,
            SignalBoundary boundary,
            BlockPos signalPosition,
            TrackGraph graph,
            int topologyVersion,
            long gameTime
    ) {
        if (boundary == null) {
            invalidateTopology();
            return remember(
                    CFRSignalAspect.RED,
                    Reason.LINK_PENDING,
                    SignalState.INVALID,
                    0,
                    0,
                    null,
                    null
            );
        }

        SignalState currentState = Objects.requireNonNullElse(
                boundary.getStateFor(signalPosition),
                SignalState.INVALID
        );
        if (currentState != SignalState.GREEN) {
            Reason reason = switch (currentState) {
                case RED -> Reason.CURRENT_RED;
                case YELLOW -> Reason.CURRENT_YELLOW;
                default -> Reason.CURRENT_INVALID;
            };
            return remember(CFRSignalAspect.RED, reason, currentState, 0, 0, null, null);
        }
        if (!family.supportsYellow()) {
            return remember(
                    CFRSignalAspect.GREEN,
                    Reason.TWO_LIGHT_CLEAR,
                    currentState,
                    0,
                    0,
                    null,
                    null
            );
        }

        Boolean side = findControlledSide(boundary, signalPosition);
        if (side == null) {
            invalidateTopology();
            return remember(CFRSignalAspect.RED, Reason.SIDE_UNRESOLVED, currentState, 0, 0, null, null);
        }
        if (graph == null) {
            invalidateTopology();
            return remember(CFRSignalAspect.RED, Reason.GRAPH_UNAVAILABLE, currentState, 0, 0, null, null);
        }

        UUID boundaryId = boundary.getId();
        if (cachedGraph != graph
                || !Objects.equals(cachedBoundaryId, boundaryId)
                || !Objects.equals(cachedSide, side)
                || cachedTopologyVersion != topologyVersion
                || gameTime >= nextTopologyRefresh) {
            cachedFollowingSignals = Map.copyOf(
                    SignalPropagator.collectChainedSignals(graph, boundary, side)
            );
            cachedGraph = graph;
            cachedBoundaryId = boundaryId;
            cachedSide = side;
            cachedTopologyVersion = topologyVersion;
            nextTopologyRefresh = gameTime
                    + TOPOLOGY_REFRESH_INTERVAL
                    + Math.floorMod(boundaryId.hashCode(), 20);
        }

        int followingCount = 0;
        int restrictiveCount = 0;
        UUID firstRestrictiveBoundary = null;
        SignalState firstRestrictiveState = null;
        boolean staleTopology = false;
        for (Map.Entry<UUID, Boolean> entry : cachedFollowingSignals.entrySet()) {
            SignalBoundary following = graph.getPoint(EdgePointType.SIGNAL, entry.getKey());
            if (following == null) {
                followingCount++;
                restrictiveCount++;
                if (firstRestrictiveBoundary == null) {
                    firstRestrictiveBoundary = entry.getKey();
                    firstRestrictiveState = SignalState.INVALID;
                }
                staleTopology = true;
                continue;
            }

            boolean followingSide = entry.getValue();
            if (following.blockEntities.get(followingSide).isEmpty()) {
                continue;
            }
            SignalState state = following.cachedStates.get(followingSide);
            state = state == null ? SignalState.INVALID : state;
            followingCount++;
            if (state != SignalState.GREEN) {
                restrictiveCount++;
                if (firstRestrictiveBoundary == null) {
                    firstRestrictiveBoundary = entry.getKey();
                    firstRestrictiveState = state;
                }
            }
        }

        Resolution resolution = restrictiveCount > 0
                ? remember(
                        CFRSignalAspect.YELLOW,
                        Reason.FOLLOWING_RESTRICTIVE,
                        currentState,
                        followingCount,
                        restrictiveCount,
                        firstRestrictiveBoundary,
                        firstRestrictiveState
                )
                : remember(
                        CFRSignalAspect.GREEN,
                        followingCount == 0 ? Reason.NO_FOLLOWING_SIGNAL : Reason.CLEAR_AHEAD,
                        currentState,
                        followingCount,
                        0,
                        null,
                        null
                );
        if (staleTopology) {
            invalidateTopology();
        }
        return resolution;
    }

    public void invalidateTopology() {
        cachedGraph = null;
        cachedBoundaryId = null;
        cachedSide = null;
        cachedTopologyVersion = Integer.MIN_VALUE;
        cachedFollowingSignals = Map.of();
        nextTopologyRefresh = Long.MIN_VALUE;
    }

    private Resolution remember(
            CFRSignalAspect aspect,
            Reason reason,
            SignalState createState,
            int followingSignals,
            int restrictiveSignals,
            UUID firstRestrictiveBoundary,
            SignalState firstRestrictiveState
    ) {
        if (lastResolution != null
                && lastResolution.aspect() == aspect
                && lastResolution.reason() == reason
                && lastResolution.createState() == createState
                && lastResolution.followingSignals() == followingSignals
                && lastResolution.restrictiveSignals() == restrictiveSignals
                && Objects.equals(lastResolution.firstRestrictiveBoundary(), firstRestrictiveBoundary)
                && lastResolution.firstRestrictiveState() == firstRestrictiveState) {
            return lastResolution;
        }
        lastResolution = new Resolution(
                aspect,
                reason,
                createState,
                followingSignals,
                restrictiveSignals,
                firstRestrictiveBoundary,
                firstRestrictiveState
        );
        return lastResolution;
    }

    public static Resolution resolveStates(
            SignalFamily family,
            SignalState currentState,
            List<FollowingSignal> followingSignals
    ) {
        SignalState current = currentState == null ? SignalState.INVALID : currentState;
        if (current != SignalState.GREEN) {
            Reason reason = switch (current) {
                case RED -> Reason.CURRENT_RED;
                case YELLOW -> Reason.CURRENT_YELLOW;
                default -> Reason.CURRENT_INVALID;
            };
            return Resolution.restrictive(reason, current);
        }

        if (!family.supportsYellow()) {
            return new Resolution(
                    CFRCreateSignalStateMapper.toRedGreenAspect(current),
                    Reason.TWO_LIGHT_CLEAR,
                    current,
                    0,
                    0,
                    null,
                    null
            );
        }

        List<FollowingSignal> following = followingSignals == null ? List.of() : followingSignals;
        int restrictiveCount = 0;
        FollowingSignal firstRestrictive = null;
        for (FollowingSignal signal : following) {
            if (signal.state() != SignalState.GREEN) {
                restrictiveCount++;
                if (firstRestrictive == null) {
                    firstRestrictive = signal;
                }
            }
        }

        if (restrictiveCount > 0) {
            return new Resolution(
                    CFRSignalAspect.YELLOW,
                    Reason.FOLLOWING_RESTRICTIVE,
                    current,
                    following.size(),
                    restrictiveCount,
                    firstRestrictive.boundaryId(),
                    firstRestrictive.state()
            );
        }

        return new Resolution(
                CFRSignalAspect.GREEN,
                following.isEmpty() ? Reason.NO_FOLLOWING_SIGNAL : Reason.CLEAR_AHEAD,
                current,
                following.size(),
                0,
                null,
                null
        );
    }

    public static Resolution manual(CFRSignalAspect aspect) {
        return new Resolution(
                Objects.requireNonNullElse(aspect, CFRSignalAspect.RED),
                Reason.MANUAL_DEBUG,
                SignalState.INVALID,
                0,
                0,
                null,
                null
        );
    }

    public static Resolution missingTrack() {
        return new Resolution(
                CFRSignalAspect.RED,
                Reason.TRACK_MISSING,
                SignalState.INVALID,
                0,
                0,
                null,
                null
        );
    }

    private static Boolean findControlledSide(SignalBoundary boundary, BlockPos signalPosition) {
        if (boundary.blockEntities.get(false).containsKey(signalPosition)) {
            return false;
        }
        if (boundary.blockEntities.get(true).containsKey(signalPosition)) {
            return true;
        }
        return null;
    }

    public record FollowingSignal(UUID boundaryId, SignalState state) {
        public FollowingSignal {
            state = state == null ? SignalState.INVALID : state;
        }
    }

    public record Resolution(
            CFRSignalAspect aspect,
            Reason reason,
            SignalState createState,
            int followingSignals,
            int restrictiveSignals,
            UUID firstRestrictiveBoundary,
            SignalState firstRestrictiveState
    ) {
        private static Resolution restrictive(Reason reason, SignalState createState) {
            return new Resolution(
                    CFRSignalAspect.RED,
                    reason,
                    createState,
                    0,
                    0,
                    null,
                    null
            );
        }
    }

    public enum Reason {
        MANUAL_DEBUG,
        TRACK_MISSING,
        LINK_PENDING,
        CURRENT_RED,
        CURRENT_YELLOW,
        CURRENT_INVALID,
        TWO_LIGHT_CLEAR,
        SIDE_UNRESOLVED,
        GRAPH_UNAVAILABLE,
        NO_FOLLOWING_SIGNAL,
        FOLLOWING_RESTRICTIVE,
        CLEAR_AHEAD;

        public String getTranslationKey() {
            return "resolver.cfrsignals." + name().toLowerCase(java.util.Locale.ROOT);
        }
    }
}
