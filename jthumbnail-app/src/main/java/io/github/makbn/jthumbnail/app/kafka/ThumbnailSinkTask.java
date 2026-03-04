package io.github.makbn.jthumbnail.app.kafka;

import io.github.makbn.jthumbnail.api.ThumbnailProcessor;

import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.Map;

/**
 * Kafka Connect Sink Task that processes records containing file paths, generates thumbnails using
 * the core library, and optionally writes results to a configured directory or topic.
 */
public class ThumbnailSinkTask extends SinkTask {

    private static final Logger log = LoggerFactory.getLogger(ThumbnailSinkTask.class);

    static final String CONFIG_TOPICS = "topics";
    static final String CONFIG_OUTPUT_DIR = "jthumbnail.output.directory";
    static final String CONFIG_RESULT_TOPIC = "jthumbnail.result.topic";

    private ThumbnailProcessor processor;
    private String outputDirectory;
    private String resultTopic;

    @Override
    public String version() {
        return "2.3.0";
    }

    @Override
    public void start(Map<String, String> props) {
        outputDirectory = props.get(CONFIG_OUTPUT_DIR);
        resultTopic = props.get(CONFIG_RESULT_TOPIC);
        processor = createProcessor(props);
        log.info("ThumbnailSinkTask started; output.directory={}, result.topic={}", outputDirectory, resultTopic);
    }

    ThumbnailProcessor createProcessor(Map<String, String> props) {
        return new CoreThumbnailProcessor();
    }

    @Override
    public void put(Collection<SinkRecord> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        for (SinkRecord record : records) {
            processRecord(record);
        }
    }

    void processRecord(SinkRecord record) {
        Object value = record.value();
        if (value == null) {
            log.warn("Skipping record with null value at offset {}", record.kafkaOffset());
            return;
        }
        String path = value.toString().trim();
        if (path.isEmpty()) {
            log.warn("Skipping record with empty path at offset {}", record.kafkaOffset());
            return;
        }
        File inputFile = new File(path);
        if (!inputFile.exists() || !inputFile.isFile()) {
            log.warn("Input file does not exist or is not a file: {}", path);
            return;
        }

        File thumbnail = processor.createThumbnail(inputFile);
        if (thumbnail != null) {
            log.info("Generated thumbnail for {} -> {}", path, thumbnail.getAbsolutePath());
            if (outputDirectory != null && !outputDirectory.isEmpty()) {
                moveOrCopyToOutputDir(thumbnail);
            }
        } else {
            log.warn("Failed to generate thumbnail for: {}", path);
        }
    }

    private void moveOrCopyToOutputDir(File thumbnail) {
        File outDir = new File(outputDirectory);
        if (!outDir.exists()) {
            if (!outDir.mkdirs()) {
                log.warn("Could not create output directory: {}", outputDirectory);
                return;
            }
        }
        File dest = new File(outDir, thumbnail.getName());
        try {
            if (thumbnail.renameTo(dest)) {
                log.debug("Moved thumbnail to {}", dest.getAbsolutePath());
            } else {
                java.nio.file.Files.copy(
                        thumbnail.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                log.debug("Copied thumbnail to {}", dest.getAbsolutePath());
            }
        } catch (Exception e) {
            log.warn("Could not move/copy thumbnail to output dir: {}", e.getMessage());
        }
    }

    @Override
    public void stop() {
        processor = null;
        log.info("ThumbnailSinkTask stopped");
    }
}
