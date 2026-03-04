package io.github.makbn.jthumbnail.app.kafka;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.sink.SinkConnector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Kafka Connect Sink Connector that consumes file paths from topics and produces thumbnails using
 * the JThumbnail core library. Each consumed record value (string path) is passed to a sink task
 * that generates a thumbnail for the file.
 */
public class ThumbnailSinkConnector extends SinkConnector {

    private Map<String, String> configProps;

    @Override
    public ConfigDef config() {
        return new ConfigDef()
                .define(
                        ThumbnailSinkTask.CONFIG_OUTPUT_DIR,
                        ConfigDef.Type.STRING,
                        null,
                        ConfigDef.Importance.LOW,
                        "Optional directory to write generated thumbnails into")
                .define(
                        ThumbnailSinkTask.CONFIG_RESULT_TOPIC,
                        ConfigDef.Type.STRING,
                        null,
                        ConfigDef.Importance.LOW,
                        "Optional topic to send thumbnail result metadata to");
    }

    @Override
    public void start(Map<String, String> props) {
        this.configProps = props;
    }

    @Override
    public Class<? extends Task> taskClass() {
        return ThumbnailSinkTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        List<Map<String, String>> configs = new ArrayList<>(maxTasks);
        for (int i = 0; i < maxTasks; i++) {
            configs.add(configProps);
        }
        return configs;
    }

    @Override
    public void stop() {
        configProps = null;
    }

    @Override
    public String version() {
        return "2.3.0";
    }
}
