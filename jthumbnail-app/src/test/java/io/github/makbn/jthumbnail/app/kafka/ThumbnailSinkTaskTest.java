package io.github.makbn.jthumbnail.app.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.makbn.jthumbnail.api.ThumbnailProcessor;

import org.apache.kafka.connect.sink.SinkRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class ThumbnailSinkTaskTest {

    @TempDir
    Path tempDir;

    private ThumbnailSinkTask task;
    private ThumbnailProcessor mockProcessor;

    @BeforeEach
    void setUp() {
        task = new ThumbnailSinkTask() {
            @Override
            ThumbnailProcessor createProcessor(Map<String, String> props) {
                return mockProcessor;
            }
        };
        mockProcessor = mock(ThumbnailProcessor.class);
    }

    @Test
    void version() {
        assertEquals("2.3.0", task.version());
    }

    @Test
    void start() {
        task.start(Map.of(
                "topics",
                "paths",
                ThumbnailSinkTask.CONFIG_OUTPUT_DIR,
                "/out",
                ThumbnailSinkTask.CONFIG_RESULT_TOPIC,
                "results"));
        assertNotNull(task);
    }

    @Test
    void putEmptyDoesNothing() {
        task.start(Map.of("topics", "t"));
        task.put(List.of());
        task.put(new ArrayList<>());
    }

    @Test
    void putSkipsNullValue() throws IOException {
        task.start(Map.of("topics", "t"));
        SinkRecord record = new SinkRecord("t", 0, null, null, null, null, 0L);
        task.put(List.of(record));
    }

    @Test
    void putSkipsEmptyPath() {
        task.start(Map.of("topics", "t"));
        SinkRecord record = new SinkRecord("t", 0, null, null, null, "   ", 0L);
        task.put(List.of(record));
    }

    @Test
    void putSkipsNonExistentFile() {
        task.start(Map.of("topics", "t"));
        SinkRecord record = new SinkRecord(
                "t", 0, null, null, null, tempDir.resolve("nonexistent.pdf").toString(), 0L);
        task.put(List.of(record));
    }

    @Test
    void putProcessesValidFileAndMovesToOutputDir() throws IOException {
        File inputFile = Files.createTempFile(tempDir, "doc", ".txt").toFile();
        Files.writeString(inputFile.toPath(), "content");
        File thumbFile = Files.createTempFile(tempDir, "thumb", ".png").toFile();
        Path outputDir = tempDir.resolve("output");
        Files.createDirectories(outputDir);

        when(mockProcessor.createThumbnail(any(File.class))).thenReturn(thumbFile);

        task.start(Map.of("topics", "t", ThumbnailSinkTask.CONFIG_OUTPUT_DIR, outputDir.toString()));
        SinkRecord record = new SinkRecord("t", 0, null, null, null, inputFile.getAbsolutePath(), 0L);
        task.put(List.of(record));

        verify(mockProcessor).createThumbnail(inputFile);
        assertEquals(1, outputDir.toFile().listFiles().length);
    }

    @Test
    void putProcessesValidFileWhenProcessorReturnsNull() throws IOException {
        File inputFile = Files.createTempFile(tempDir, "doc", ".txt").toFile();
        Files.writeString(inputFile.toPath(), "content");
        when(mockProcessor.createThumbnail(any(File.class))).thenReturn(null);

        task.start(Map.of("topics", "t"));
        SinkRecord record = new SinkRecord("t", 0, null, null, null, inputFile.getAbsolutePath(), 0L);
        task.put(List.of(record));

        verify(mockProcessor).createThumbnail(inputFile);
    }

    @Test
    void putWithOutputDirCreatesDirIfMissing() throws IOException {
        File inputFile = Files.createTempFile(tempDir, "doc", ".txt").toFile();
        Files.writeString(inputFile.toPath(), "x");
        File thumbFile = Files.createTempFile(tempDir, "thumb", ".png").toFile();
        Path outputDir = tempDir.resolve("newdir");

        when(mockProcessor.createThumbnail(any(File.class))).thenReturn(thumbFile);

        task.start(Map.of("topics", "t", ThumbnailSinkTask.CONFIG_OUTPUT_DIR, outputDir.toString()));
        SinkRecord record = new SinkRecord("t", 0, null, null, null, inputFile.getAbsolutePath(), 0L);
        task.put(List.of(record));

        assertEquals(1, outputDir.toFile().listFiles().length);
    }

    @Test
    void stop() {
        task.start(Map.of("topics", "t"));
        task.stop();
    }

    @Test
    void putWithEmptyOutputDirDoesNotMove() throws IOException {
        File inputFile = Files.createTempFile(tempDir, "doc", ".txt").toFile();
        Files.writeString(inputFile.toPath(), "x");
        File thumbFile = Files.createTempFile(tempDir, "thumb", ".png").toFile();
        when(mockProcessor.createThumbnail(any(File.class))).thenReturn(thumbFile);

        task.start(Map.of("topics", "t", ThumbnailSinkTask.CONFIG_OUTPUT_DIR, ""));
        SinkRecord record = new SinkRecord("t", 0, null, null, null, inputFile.getAbsolutePath(), 0L);
        task.put(List.of(record));

        verify(mockProcessor).createThumbnail(inputFile);
    }

    @Test
    void putWhenOutputDirIsFileSkipsMove() throws IOException {
        File inputFile = Files.createTempFile(tempDir, "doc", ".txt").toFile();
        Files.writeString(inputFile.toPath(), "x");
        File thumbFile = Files.createTempFile(tempDir, "thumb", ".png").toFile();
        File outputAsFile = tempDir.resolve("output").toFile();
        Files.writeString(outputAsFile.toPath(), "not a dir");
        when(mockProcessor.createThumbnail(any(File.class))).thenReturn(thumbFile);

        task.start(Map.of("topics", "t", ThumbnailSinkTask.CONFIG_OUTPUT_DIR, outputAsFile.getAbsolutePath()));
        SinkRecord record = new SinkRecord("t", 0, null, null, null, inputFile.getAbsolutePath(), 0L);
        task.put(List.of(record));

        verify(mockProcessor).createThumbnail(inputFile);
    }

    @Test
    void putWhenRenameFailsUsesCopy() throws IOException {
        File inputFile = Files.createTempFile(tempDir, "doc", ".txt").toFile();
        Files.writeString(inputFile.toPath(), "x");
        Path thumbPath = Files.createTempFile(tempDir, "thumb", ".png");
        File thumbFile = thumbPath.toFile();
        Path outputDir = tempDir.resolve("out");
        Files.createDirectories(outputDir);
        when(mockProcessor.createThumbnail(any(File.class))).thenReturn(thumbFile);

        task.start(Map.of("topics", "t", ThumbnailSinkTask.CONFIG_OUTPUT_DIR, outputDir.toString()));
        SinkRecord record = new SinkRecord("t", 0, null, null, null, inputFile.getAbsolutePath(), 0L);
        task.put(List.of(record));

        assertEquals(1, outputDir.toFile().listFiles().length);
    }

    @Test
    void putWhenOutputDirNotWritableLogsAndContinues() throws IOException {
        File inputFile = Files.createTempFile(tempDir, "doc", ".txt").toFile();
        Files.writeString(inputFile.toPath(), "x");
        Path outputDir = tempDir.resolve("readonly");
        Files.createDirectories(outputDir);
        outputDir.toFile().setReadOnly();
        File thumbFile = Files.createTempFile(tempDir, "thumb", ".png").toFile();
        when(mockProcessor.createThumbnail(any(File.class))).thenReturn(thumbFile);

        task.start(Map.of("topics", "t", ThumbnailSinkTask.CONFIG_OUTPUT_DIR, outputDir.toString()));
        SinkRecord record = new SinkRecord("t", 0, null, null, null, inputFile.getAbsolutePath(), 0L);
        task.put(List.of(record));

        verify(mockProcessor).createThumbnail(inputFile);
    }
}
