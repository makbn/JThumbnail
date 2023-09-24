package io.github.makbn.jthumbnail.core.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.ResourceBundle;

/**
 * created by Mehdi Akbarian-Rastaghi 2018-10-21
 */
@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AppSettings {
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
        openOfficePorts = Arrays.stream(getValue("jthumbnailer.openoffice.port").split(",")).mapToInt(Integer::valueOf).toArray();
        openOfficePath = getValue("jthumbnailer.openoffice.dir");
        thumbHeight = Integer.parseInt(getValue("jthumbnailer.thumb_height"));
        thumbWidth = Integer.parseInt(getValue("jthumbnailer.thumb_width"));
        asyncCorePoolSize = Integer.parseInt(getValue("jthumbnailer.async.core_pool_size"));
        asyncMaxPoolSize = Integer.parseInt(getValue("jthumbnailer.async.max_pool_size"));
        timeout = Long.parseLong(getValue("jthumbnailer.openoffice.timeout"));
        maxTaskPerProcess = Integer.parseInt(getValue("jthumbnailer.openoffice.max_tasks_per_process"));
        officeTemporaryDirectory = String.format("%s/office/%d/", getValue("jthumbnailer.openoffice.tmp"), System.currentTimeMillis());
        uploadTemporaryDirectory = String.format("%s/upload/%d/", getValue("jthumbnailer.openoffice.tmp"), System.currentTimeMillis());
    }

    private String getValue(String key) {
        return rb.getString(key);
    }

    public File getUploadDirectory() throws IOException {
        File uploadDirectory = new File(uploadTemporaryDirectory);
        if (!uploadDirectory.exists()) {
            uploadDirectory = Files.createDirectories(Path.of(uploadTemporaryDirectory)).toFile();
        }
        return uploadDirectory;
    }

    public File getOfficeDirectory() throws IOException {
        File officeDirectory = new File(officeTemporaryDirectory);
        if (!officeDirectory.exists()) {
            officeDirectory = Files.createDirectories(Path.of(officeTemporaryDirectory)).toFile();
        }
        return officeDirectory;
    }
}
