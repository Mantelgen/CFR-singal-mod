package ro.cfrsignals.railway;

import com.simibubi.create.content.trains.signal.SignalBlockEntity.SignalState;
import ro.cfrsignals.signal.CFRSignalAspect;

public final class CFRCreateSignalStateMapper {
    private CFRCreateSignalStateMapper() {
    }

    public static CFRSignalAspect toRedGreenAspect(SignalState state) {
        return state == SignalState.GREEN ? CFRSignalAspect.GREEN : CFRSignalAspect.RED;
    }
}
