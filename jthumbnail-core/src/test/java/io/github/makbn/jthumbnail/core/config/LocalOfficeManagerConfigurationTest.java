package io.github.makbn.jthumbnail.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.makbn.jthumbnail.core.exception.ThumbnailRuntimeException;
import io.github.makbn.jthumbnail.core.properties.LocalOfficeProperties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.io.File;

/**
 * Tests for LocalOfficeManagerConfiguration, especially validation (e.g. working dir must be a
 * directory).
 */
class LocalOfficeManagerConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(LocalOfficeManagerConfiguration.class, PropertiesConfig.class);

    @Test
    @DisplayName("getOfficeManager throws when working dir is a file, not a directory")
    void getOfficeManagerThrowsWhenWorkingDirIsFile() throws Exception {
        File tempFile = File.createTempFile("jthumb-test", ".tmp");
        tempFile.deleteOnExit();
        String workingDirPath = tempFile.getAbsolutePath();

        Exception ex = assertThrows(Exception.class, () -> runner.withPropertyValues(
                        "jthumbnailer.openoffice.manager_type=local",
                        "jthumbnailer.openoffice.ports=2002",
                        "jthumbnailer.openoffice.working-dir=" + workingDirPath)
                .run(ctx -> ctx.getBean(org.jodconverter.core.office.OfficeManager.class)));
        assertTrue(
                isCauseOrSelf(ex, ThumbnailRuntimeException.class) || isCauseOrSelf(ex, java.io.IOException.class),
                "expected ThumbnailRuntimeException or IOException in cause chain: " + ex);
    }

    @Test
    @DisplayName("LocalOfficeProperties binds when working dir is a valid directory")
    void localOfficePropertiesBindsWithValidWorkingDir() throws Exception {
        File tempDir = java.nio.file.Files.createTempDirectory("jthumb-test").toFile();
        tempDir.deleteOnExit();
        String workingDirPath = tempDir.getAbsolutePath();

        new ApplicationContextRunner()
                .withUserConfiguration(PropertiesConfigOnly.class)
                .withPropertyValues(
                        "jthumbnailer.openoffice.manager_type=local",
                        "jthumbnailer.openoffice.ports=2002",
                        "jthumbnailer.openoffice.working-dir=" + workingDirPath)
                .run(ctx -> {
                    LocalOfficeProperties props = ctx.getBean(LocalOfficeProperties.class);
                    assertNotNull(props);
                    assertEquals(io.github.makbn.jthumbnail.core.properties.ManagerType.LOCAL, props.managerType());
                    assertNotNull(props.workingDir());
                });
    }

    private static boolean isCauseOrSelf(Throwable t, Class<? extends Throwable> type) {
        for (Throwable c = t; c != null; c = c.getCause()) {
            if (type.isInstance(c)) return true;
        }
        return false;
    }

    @Configuration
    @EnableConfigurationProperties(LocalOfficeProperties.class)
    @Import(LocalOfficeManagerConfiguration.class)
    static class PropertiesConfig {}

    @Configuration
    @EnableConfigurationProperties(LocalOfficeProperties.class)
    static class PropertiesConfigOnly {}
}
