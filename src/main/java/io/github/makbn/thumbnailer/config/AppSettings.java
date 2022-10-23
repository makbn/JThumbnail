package io.github.makbn.thumbnailer.config;

import java.util.Arrays;
import java.util.ResourceBundle;

/**
 * created by Mehdi Akbarian-Rastaghi 2018-10-21
 */
public class AppSettings {
    private final ResourceBundle rb = ResourceBundle.getBundle("application");

    /**
     * How long may a service work take? (in ms)
     */
    private long timeout;
    private int[] openOfficePorts;
    private String openOfficePath;
    private int maxTaskPerProcess;
    private int thumbWidth;
    private int thumbHeight;
    private int asyncCorePoolSize;
    private int asyncMaxPoolSize;

    private String officeTemporaryDirectory;

    public AppSettings() {
        config();
    }

    private void config() {
        openOfficePorts = Arrays.stream(getValue("jthumbnailer.openoffice.port").split(",")).mapToInt(Integer::valueOf).toArray();
        openOfficePath = getValue("jthumbnailer.openoffice.dir");
        thumbHeight = Integer.parseInt(getValue("jthumbnailer.thumb_height"));
        thumbWidth = Integer.parseInt(getValue("jthumbnailer.thumb_width"));
        asyncCorePoolSize = Integer.parseInt(getValue("jthumbnailer.async.core_pool_size"));
        asyncMaxPoolSize = Integer.parseInt(getValue("jthumbnailer.async.max_pool_size"));
        timeout = Long.parseLong(getValue("jthumbnailer.openoffice.timeout"));
        maxTaskPerProcess = Integer.parseInt(getValue("jthumbnailer.openoffice.max_tasks_per_process"));
        officeTemporaryDirectory = String.format("%s/%d/",getValue("jthumbnailer.openoffice.tmp"), System.currentTimeMillis());
    }

    private String getValue(String key) {
        return rb.getString(key);
    }

    public int[] getOpenOfficePorts() {
        return openOfficePorts;
    }

    public String getOpenOfficePath() {
        return openOfficePath;
    }

    public int getThumbWidth() {
        return thumbWidth;
    }

    public int getThumbHeight() {
        return thumbHeight;
    }

    public long getTimeout() {
        return timeout;
    }

    public int getMaxTaskPerProcess() {
        return maxTaskPerProcess;
    }

    public int getAsyncCorePoolSize() {
        return asyncCorePoolSize;
    }

    public int getAsyncMaxPoolSize() {
        return asyncMaxPoolSize;
    }

    public String getOfficeTemporaryDirectory() {
        return officeTemporaryDirectory;
    }
}
