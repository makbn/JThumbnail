package io.github.makbn.jthumbnail.core.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

/**
 * created by Mehdi Akbarian-Rastaghi 2018-10-21
 */
@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AppSettings {
    public static final String JTHUMBNAILER_OPENOFFICE_PORT = "jthumbnailer.openoffice.port";
    public static final String JTHUMBNAILER_OPENOFFICE_DIR = "jthumbnailer.openoffice.dir";
    public static final String JTHUMBNAILER_THUMB_HEIGHT = "jthumbnailer.thumb_height";
    public static final String JTHUMBNAILER_THUMB_WIDTH = "jthumbnailer.thumb_width";
    public static final String JTHUMBNAILER_ASYNC_CORE_POOL_SIZE = "jthumbnailer.async.core_pool_size";
    public static final String JTHUMBNAILER_ASYNC_MAX_POOL_SIZE = "jthumbnailer.async.max_pool_size";
    public static final String JTHUMBNAILER_OPENOFFICE_TIMEOUT = "jthumbnailer.openoffice.timeout";
    public static final String JTHUMBNAILER_OPENOFFICE_MAX_TASKS_PER_PROCESS =
            "jthumbnailer.openoffice.max_tasks_per_process";
    public static final String JTHUMBNAILER_OPENOFFICE_TMP = "jthumbnailer.openoffice.tmp";

    private final ResourceBundle rb = ResourceBundle.getBundle("application");

    /**
     * How long may a service work take? (in ms)
     */
    long timeout;

    int[] openOfficePorts;
    String openOfficePath;
    int maxTaskPerProcess;
    int thumbWidth;
    int thumbHeight;
    int asyncCorePoolSize;
    int asyncMaxPoolSize;

    String officeTemporaryDirectory;
    String uploadTemporaryDirectory;

    public AppSettings() {
        openOfficePorts = Arrays.stream(getValue(JTHUMBNAILER_OPENOFFICE_PORT).split(","))
                .mapToInt(Integer::valueOf)
                .toArray();
        openOfficePath = getValue(JTHUMBNAILER_OPENOFFICE_DIR);
        thumbHeight = Integer.parseInt(getValue(JTHUMBNAILER_THUMB_HEIGHT));
        thumbWidth = Integer.parseInt(getValue(JTHUMBNAILER_THUMB_WIDTH));
        asyncCorePoolSize = Integer.parseInt(getValue(JTHUMBNAILER_ASYNC_CORE_POOL_SIZE));
        asyncMaxPoolSize = Integer.parseInt(getValue(JTHUMBNAILER_ASYNC_MAX_POOL_SIZE));
        timeout = Long.parseLong(getValue(JTHUMBNAILER_OPENOFFICE_TIMEOUT));
        maxTaskPerProcess = Integer.parseInt(getValue(JTHUMBNAILER_OPENOFFICE_MAX_TASKS_PER_PROCESS));
        officeTemporaryDirectory =
                String.format("%s/office/%d/", getValue(JTHUMBNAILER_OPENOFFICE_TMP), System.currentTimeMillis());
        uploadTemporaryDirectory =
                String.format("%s/upload/%d/", getValue(JTHUMBNAILER_OPENOFFICE_TMP), System.currentTimeMillis());
    }

    private String getValue(@NonNull String key) {
        String envKey = Arrays.stream(key.split("\\."))
                .map(part -> part.substring(0, 1).toUpperCase() + part.substring(1))
                .collect(Collectors.joining(""));

        String envValue = System.getenv(envKey);
        return envValue != null ? envValue : rb.getString(key);
    }

    public File getUploadDirectory() throws IOException {
        File uploadDirectory = new File(uploadTemporaryDirectory);
        if (!uploadDirectory.exists()) {
            uploadDirectory =
                    Files.createDirectories(Path.of(uploadTemporaryDirectory)).toFile();
        }
        return uploadDirectory;
    }

    public File getOfficeDirectory() throws IOException {
        File officeDirectory = new File(officeTemporaryDirectory);
        if (!officeDirectory.exists()) {
            officeDirectory =
                    Files.createDirectories(Path.of(officeTemporaryDirectory)).toFile();
        }
        return officeDirectory;
    }
}
