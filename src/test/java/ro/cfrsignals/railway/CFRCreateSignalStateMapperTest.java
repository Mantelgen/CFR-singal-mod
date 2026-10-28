package ro.cfrsignals.railway;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.simibubi.create.content.trains.signal.SignalBlockEntity.SignalState;
import org.junit.jupiter.api.Test;
import ro.cfrsignals.signal.CFRSignalAspect;

final class CFRCreateSignalStateMapperTest {
    @Test
    void clearCreateSignalMapsToGreen() {
        assertEquals(
                CFRSignalAspect.GREEN,
                CFRCreateSignalStateMapper.toRedGreenAspect(SignalState.GREEN)
        );
    }

    @Test
    void everyRestrictiveOrInvalidStateMapsToRed() {
        assertEquals(CFRSignalAspect.RED, CFRCreateSignalStateMapper.toRedGreenAspect(SignalState.RED));
        assertEquals(CFRSignalAspect.RED, CFRCreateSignalStateMapper.toRedGreenAspect(SignalState.YELLOW));
        assertEquals(CFRSignalAspect.RED, CFRCreateSignalStateMapper.toRedGreenAspect(SignalState.INVALID));
        assertEquals(CFRSignalAspect.RED, CFRCreateSignalStateMapper.toRedGreenAspect(null));
    }
}
