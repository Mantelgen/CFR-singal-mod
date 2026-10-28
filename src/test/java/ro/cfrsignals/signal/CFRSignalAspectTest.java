package ro.cfrsignals.signal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class CFRSignalAspectTest {
    @Test
    void twoLightDebugCycleSkipsYellow() {
        assertEquals(CFRSignalAspect.GREEN, CFRSignalAspect.RED.nextDebugAspect(false));
        assertEquals(CFRSignalAspect.RED, CFRSignalAspect.GREEN.nextDebugAspect(false));
        assertEquals(CFRSignalAspect.RED, CFRSignalAspect.YELLOW.nextDebugAspect(false));
    }

    @Test
    void threeLightDebugCycleIncludesYellow() {
        assertEquals(CFRSignalAspect.YELLOW, CFRSignalAspect.RED.nextDebugAspect(true));
        assertEquals(CFRSignalAspect.GREEN, CFRSignalAspect.YELLOW.nextDebugAspect(true));
        assertEquals(CFRSignalAspect.RED, CFRSignalAspect.GREEN.nextDebugAspect(true));
    }
}
