package io.github.makbn.thumbnailer.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jodconverter.local.process.UnixProcessManager;


public class MacProcessManager extends UnixProcessManager {
    private static final Logger mLog = LogManager.getLogger(MacProcessManager.class);

    @Override
    protected String[] getRunningProcessesCommand(String process) {
        return new String[]{"/bin/bash", "-c", "/bin/ps -e -o pid,command | /usr/bin/grep " + process};
    }
}
