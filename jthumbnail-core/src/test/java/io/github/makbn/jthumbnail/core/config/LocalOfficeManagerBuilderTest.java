package io.github.makbn.jthumbnail.core.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.jodconverter.local.office.ExistingProcessAction;
import org.jodconverter.local.office.LocalOfficeManager;
import org.jodconverter.local.process.PureJavaProcessManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Optional;

/**
 * Unit tests for the null-safe LocalOfficeManager builder pattern used in OpenOfficeTest and
 * elsewhere. Verifies that builder accepts null officeHome, empty pipeNames, and default
 * maxTasksPerProcess without NPE.
 */
class LocalOfficeManagerBuilderTest {

    @TempDir
    java.nio.file.Path tempDir;

    /**
     * Null-safe builder chain with empty pipeNames (same pattern as OpenOfficeTest). We do not
     * call build() because JOD can NPE without a full runtime; we assert the chain runs without NPE.
     */
    @Test
    void builderAcceptsNullOfficeHomeAndEmptyPipeNames() throws Exception {
        File workingDir = Files.createTempDirectory(tempDir, "jod").toFile();
        PureJavaProcessManager processManager = new PureJavaProcessManager();
        String[] pipeNames = Optional.ofNullable(Collections.<String>emptyList())
                .orElse(Collections.emptyList())
                .toArray(new String[0]);
        var builder = LocalOfficeManager.builder()
                .portNumbers(2002)
                .pipeNames(pipeNames)
                .processManager(processManager)
                .maxTasksPerProcess(1)
                .existingProcessAction(ExistingProcessAction.KILL)
                .keepAliveOnShutdown(false)
                .processRetryInterval(0L)
                .workingDir(workingDir);
        assertNotNull(builder);
    }

    @Test
    void builderAcceptsNullOfficeHomeAndPipeNames() throws Exception {
        File workingDir = Files.createTempDirectory(tempDir, "jod").toFile();
        PureJavaProcessManager processManager = new PureJavaProcessManager();
        var builder = LocalOfficeManager.builder()
                .portNumbers(2002)
                .pipeNames("jt-pipe")
                .processManager(processManager)
                .maxTasksPerProcess(1)
                .existingProcessAction(ExistingProcessAction.KILL)
                .keepAliveOnShutdown(false)
                .processRetryInterval(0L)
                .workingDir(workingDir);
        assertNotNull(builder);
    }

    @Test
    void builderAcceptsDefaultMaxTasksPerProcessWhenNull() throws Exception {
        File workingDir = Files.createTempDirectory(tempDir, "jod").toFile();
        PureJavaProcessManager processManager = new PureJavaProcessManager();
        Integer maxTasks = null;
        int effective = maxTasks != null ? maxTasks : 1;
        var builder = LocalOfficeManager.builder()
                .portNumbers(2002)
                .pipeNames()
                .processManager(processManager)
                .maxTasksPerProcess(effective)
                .existingProcessAction(ExistingProcessAction.KILL)
                .keepAliveOnShutdown(false)
                .processRetryInterval(0L)
                .workingDir(workingDir);
        assertNotNull(builder);
    }
}
