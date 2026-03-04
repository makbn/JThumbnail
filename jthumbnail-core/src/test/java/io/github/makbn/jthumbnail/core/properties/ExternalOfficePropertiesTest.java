package io.github.makbn.jthumbnail.core.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

class ExternalOfficePropertiesTest {

    private ExternalOfficeProperties createProperties() {
        File workingDir = new File(System.getProperty("java.io.tmpdir"), "jthumb-external-test");
        return new ExternalOfficeProperties(
                ManagerType.EXTERNAL,
                "127.0.0.1",
                List.of(2002, 2003),
                null,
                null,
                120000L,
                1000,
                30000L,
                120000L,
                false,
                true,
                250L,
                workingDir);
    }

    @Test
    void managerTypeIsExternal() {
        ExternalOfficeProperties properties = createProperties();
        assertEquals(ManagerType.EXTERNAL, properties.managerType());
    }

    @Test
    void hostnameBound() {
        ExternalOfficeProperties properties = createProperties();
        assertEquals("127.0.0.1", properties.hostname());
    }

    @Test
    void portsBound() {
        ExternalOfficeProperties properties = createProperties();
        assertNotNull(properties.ports());
        assertEquals(2, properties.ports().size());
        assertEquals(List.of(2002, 2003), properties.ports());
    }

    @Test
    void workingDirBound() {
        ExternalOfficeProperties properties = createProperties();
        assertNotNull(properties.workingDir());
        assertTrue(
                properties.workingDir().getPath().contains("jthumb-external-test"),
                "workingDir should contain jthumb-external-test");
    }

    @Test
    void defaultsApplied() {
        ExternalOfficeProperties properties = createProperties();
        assertTrue(properties.connectOnStart());
        assertFalse(properties.failFast());
        assertEquals(120000L, properties.connectionTimeout());
        assertEquals(1000, properties.maxTasksPerConnection());
        assertEquals(30000L, properties.taskQueueTimeout());
        assertEquals(120000L, properties.taskExecutionTimeout());
        assertEquals(250L, properties.connectRetryInterval());
    }

    @Test
    void optionalFieldsCanBeNull() {
        ExternalOfficeProperties properties = createProperties();
        assertNull(properties.pipeNames());
        assertNull(properties.websocketUrls());
    }
}
