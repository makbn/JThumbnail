package io.github.makbn.thumbnailer;

import org.apache.tika.utils.SystemUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OsTest {

    @Test
    void is_mac() {
        assertTrue(SystemUtils.IS_OS_MAC);
        assertTrue(SystemUtils.IS_OS_MAC_OSX);
    }
}
