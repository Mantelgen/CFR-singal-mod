package ro.cfrsignals.railway;

import java.util.UUID;

import com.simibubi.create.content.trains.signal.SignalBoundary;
import com.simibubi.create.content.trains.track.TrackTargetingBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public record CFRSignalLinkSnapshot(
        Status status,
        BlockPos trackPosition,
        Direction.AxisDirection targetDirection,
        UUID boundaryId,
        boolean curve
) {
    public static CFRSignalLinkSnapshot from(
            TrackTargetingBehaviour<SignalBoundary> targeting,
            BlockPos controllerPosition
    ) {
        if (targeting == null) {
            return new CFRSignalLinkSnapshot(Status.UNLINKED, null, null, null, false);
        }

        BlockPos trackPosition = targeting.getGlobalPosition().immutable();
        Direction.AxisDirection direction = targeting.getTargetDirection();
        boolean configuredTarget = !trackPosition.equals(controllerPosition)
                || targeting.getTargetBezier() != null;
        if (!targeting.hasValidTrack()) {
            return configuredTarget
                    ? new CFRSignalLinkSnapshot(
                            Status.TRACK_MISSING,
                            trackPosition,
                            direction,
                            null,
                            targeting.isOnCurve()
                    )
                    : new CFRSignalLinkSnapshot(Status.UNLINKED, null, null, null, false);
        }

        SignalBoundary boundary = targeting.getEdgePoint();
        if (boundary == null) {
            return new CFRSignalLinkSnapshot(
                    Status.TARGET_PENDING,
                    trackPosition,
                    direction,
                    null,
                    targeting.isOnCurve()
            );
        }

        return new CFRSignalLinkSnapshot(
                Status.LINKED,
                trackPosition,
                direction,
                boundary.getId(),
                targeting.isOnCurve()
        );
    }

    public enum Status {
        UNLINKED,
        TRACK_MISSING,
        TARGET_PENDING,
        LINKED
    }
}
