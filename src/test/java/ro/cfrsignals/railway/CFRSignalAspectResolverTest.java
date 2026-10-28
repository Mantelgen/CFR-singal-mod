package ro.cfrsignals.railway;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;

import com.simibubi.create.content.trains.signal.SignalBlockEntity.SignalState;
import org.junit.jupiter.api.Test;
import ro.cfrsignals.config.CFRSignalConfiguration.SignalFamily;
import ro.cfrsignals.railway.CFRSignalAspectResolver.FollowingSignal;
import ro.cfrsignals.railway.CFRSignalAspectResolver.Reason;
import ro.cfrsignals.signal.CFRSignalAspect;

final class CFRSignalAspectResolverTest {
    private static final UUID NEXT_SIGNAL = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID BRANCH_SIGNAL = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test
    void currentRestrictiveStateAlwaysStops() {
        var red = resolve(SignalFamily.THREE_LIGHT, SignalState.RED, SignalState.GREEN);
        var yellow = resolve(SignalFamily.THREE_LIGHT, SignalState.YELLOW, SignalState.GREEN);
        var invalid = resolve(SignalFamily.THREE_LIGHT, SignalState.INVALID, SignalState.GREEN);

        assertEquals(CFRSignalAspect.RED, red.aspect());
        assertEquals(Reason.CURRENT_RED, red.reason());
        assertEquals(CFRSignalAspect.RED, yellow.aspect());
        assertEquals(Reason.CURRENT_YELLOW, yellow.reason());
        assertEquals(CFRSignalAspect.RED, invalid.aspect());
        assertEquals(Reason.CURRENT_INVALID, invalid.reason());
    }

    @Test
    void restrictiveFollowingSectionProducesYellow() {
        for (SignalState state : List.of(SignalState.RED, SignalState.YELLOW, SignalState.INVALID)) {
            var resolution = resolve(SignalFamily.THREE_LIGHT, SignalState.GREEN, state);

            assertEquals(CFRSignalAspect.YELLOW, resolution.aspect());
            assertEquals(Reason.FOLLOWING_RESTRICTIVE, resolution.reason());
            assertEquals(1, resolution.followingSignals());
            assertEquals(1, resolution.restrictiveSignals());
            assertEquals(NEXT_SIGNAL, resolution.firstRestrictiveBoundary());
            assertEquals(state, resolution.firstRestrictiveState());
        }
    }

    @Test
    void clearFollowingSectionProducesGreen() {
        var resolution = resolve(SignalFamily.THREE_LIGHT, SignalState.GREEN, SignalState.GREEN);

        assertEquals(CFRSignalAspect.GREEN, resolution.aspect());
        assertEquals(Reason.CLEAR_AHEAD, resolution.reason());
        assertEquals(1, resolution.followingSignals());
        assertEquals(0, resolution.restrictiveSignals());
    }

    @Test
    void restrictiveBranchWinsAtJunction() {
        var resolution = CFRSignalAspectResolver.resolveStates(
                SignalFamily.THREE_LIGHT,
                SignalState.GREEN,
                List.of(
                        new FollowingSignal(NEXT_SIGNAL, SignalState.GREEN),
                        new FollowingSignal(BRANCH_SIGNAL, SignalState.RED)
                )
        );

        assertEquals(CFRSignalAspect.YELLOW, resolution.aspect());
        assertEquals(2, resolution.followingSignals());
        assertEquals(1, resolution.restrictiveSignals());
        assertEquals(BRANCH_SIGNAL, resolution.firstRestrictiveBoundary());
    }

    @Test
    void openEndedClearSectionProducesGreen() {
        var resolution = CFRSignalAspectResolver.resolveStates(
                SignalFamily.THREE_LIGHT,
                SignalState.GREEN,
                List.of()
        );

        assertEquals(CFRSignalAspect.GREEN, resolution.aspect());
        assertEquals(Reason.NO_FOLLOWING_SIGNAL, resolution.reason());
    }

    @Test
    void twoLightFamilyIgnoresAdvanceState() {
        var resolution = resolve(SignalFamily.TWO_LIGHT, SignalState.GREEN, SignalState.RED);

        assertEquals(CFRSignalAspect.GREEN, resolution.aspect());
        assertEquals(Reason.TWO_LIGHT_CLEAR, resolution.reason());
        assertEquals(0, resolution.followingSignals());
    }

    @Test
    void mechanicalSemaphoreUsesCurrentSectionOnly() {
        var clear = resolve(SignalFamily.MECHANICAL_SEMAPHORE, SignalState.GREEN, SignalState.RED);
        var stop = resolve(SignalFamily.MECHANICAL_SEMAPHORE, SignalState.RED, SignalState.GREEN);

        assertEquals(CFRSignalAspect.GREEN, clear.aspect());
        assertEquals(Reason.TWO_LIGHT_CLEAR, clear.reason());
        assertEquals(CFRSignalAspect.RED, stop.aspect());
        assertEquals(Reason.CURRENT_RED, stop.reason());
    }

    @Test
    void missingTargetedTrackFailsSafeToRed() {
        var resolution = CFRSignalAspectResolver.missingTrack();

        assertEquals(CFRSignalAspect.RED, resolution.aspect());
        assertEquals(SignalState.INVALID, resolution.createState());
        assertEquals(Reason.TRACK_MISSING, resolution.reason());
    }

    private static CFRSignalAspectResolver.Resolution resolve(
            SignalFamily family,
            SignalState current,
            SignalState following
    ) {
        return CFRSignalAspectResolver.resolveStates(
                family,
                current,
                List.of(new FollowingSignal(NEXT_SIGNAL, following))
        );
    }
}
