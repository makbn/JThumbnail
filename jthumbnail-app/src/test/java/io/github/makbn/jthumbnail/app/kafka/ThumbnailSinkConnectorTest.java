package io.github.makbn.jthumbnail.app.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.kafka.common.config.ConfigDef;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class ThumbnailSinkConnectorTest {

    private final ThumbnailSinkConnector connector = new ThumbnailSinkConnector();

    @Test
    void version() {
        assertEquals("2.3.0", connector.version());
    }

    @Test
    void taskClass() {
        assertEquals(ThumbnailSinkTask.class, connector.taskClass());
    }

    @Test
    void startAndTaskConfigs() {
        Map<String, String> props = Map.of(
                "topics",
                "file-paths",
                ThumbnailSinkTask.CONFIG_OUTPUT_DIR,
                "/tmp/thumb",
                ThumbnailSinkTask.CONFIG_RESULT_TOPIC,
                "thumb-results");
        connector.start(props);
        List<Map<String, String>> configs = connector.taskConfigs(3);
        assertNotNull(configs);
        assertEquals(3, configs.size());
        for (Map<String, String> config : configs) {
            assertEquals("file-paths", config.get("topics"));
            assertEquals("/tmp/thumb", config.get(ThumbnailSinkTask.CONFIG_OUTPUT_DIR));
            assertEquals("thumb-results", config.get(ThumbnailSinkTask.CONFIG_RESULT_TOPIC));
        }
    }

    @Test
    void stopClearsConfig() {
        connector.start(Map.of("topics", "t"));
        connector.stop();
        List<Map<String, String>> configs = connector.taskConfigs(1);
        assertNotNull(configs);
        assertEquals(1, configs.size());
        assertEquals(null, configs.get(0));
    }

    @Test
    void config() {
        ConfigDef def = connector.config();
        assertNotNull(def);
        assertNotNull(def.configKeys().get(ThumbnailSinkTask.CONFIG_OUTPUT_DIR));
        assertNotNull(def.configKeys().get(ThumbnailSinkTask.CONFIG_RESULT_TOPIC));
    }
}
