package io.github.makbn.jthumbnail;

import org.apache.tika.utils.SystemUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PlatformTest {

    @Test
    void testIsOSMac() {
        assertTrue(SystemUtils.IS_OS_MAC);
        assertTrue(SystemUtils.IS_OS_MAC_OSX);
    }
}
