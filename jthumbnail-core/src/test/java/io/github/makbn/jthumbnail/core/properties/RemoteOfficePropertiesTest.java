package io.github.makbn.jthumbnail.core.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;

class RemoteOfficePropertiesTest {

    private RemoteOfficeProperties createProperties() {
        File workingDir = new File(System.getProperty("java.io.tmpdir"), "jthumb-remote-test");
        return new RemoteOfficeProperties(
                ManagerType.REMOTE,
                workingDir,
                1,
                URI.create("http://localhost:9980"),
                120000L,
                120000L,
                1000,
                30000L,
                120000L);
    }

    @Test
    void managerTypeIsRemote() {
        RemoteOfficeProperties properties = createProperties();
        assertEquals(ManagerType.REMOTE, properties.managerType());
    }

    @Test
    void urlConnectionBound() {
        RemoteOfficeProperties properties = createProperties();
        assertEquals(URI.create("http://localhost:9980"), properties.urlConnection());
    }

    @Test
    void workingDirBound() {
        RemoteOfficeProperties properties = createProperties();
        assertNotNull(properties.workingDir());
        assertEquals(
                "jthumb-remote-test",
                properties.workingDir().getName(),
                "workingDir name should be jthumb-remote-test");
    }

    @Test
    void defaultsApplied() {
        RemoteOfficeProperties properties = createProperties();
        assertEquals(1, properties.poolSize());
        assertEquals(120000L, properties.connectionTimeout());
        assertEquals(120000L, properties.socketTimeout());
        assertEquals(1000, properties.maxTasksPerConnection());
        assertEquals(30000L, properties.taskQueueTimeout());
        assertEquals(120000L, properties.taskExecutionTimeout());
    }
}
