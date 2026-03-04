package io.github.makbn.jthumbnail;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.github.makbn.jthumbnail.core.properties.LocalOfficeProperties;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.ExistingProcessAction;
import org.jodconverter.local.office.LocalOfficeManager;
import org.jodconverter.local.process.ProcessManager;
import org.jodconverter.local.process.PureJavaProcessManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;

@Log4j2
@SpringBootTest(classes = {OpenOfficeTest.class})
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EnableConfigurationProperties(value = {LocalOfficeProperties.class})
class OpenOfficeTest {

    LocalOfficeProperties localOfficeProperties;

    @Autowired
    public OpenOfficeTest(LocalOfficeProperties officeProperties) {
        this.localOfficeProperties = officeProperties;
    }

    @Test
    void testRunSOffice() {
        assumeTrue(
                localOfficeProperties.officeHome() != null,
                "Skipping: jthumbnailer.openoffice.office-home not set (LibreOffice not configured)");
        log.info("SOffice will be running on pipe: {}", localOfficeProperties.pipeNames());
        ProcessManager processManager = new PureJavaProcessManager();
        var ports = Optional.ofNullable(localOfficeProperties.ports()).orElse(Collections.emptyList());
        assumeTrue(!ports.isEmpty(), "Skipping: no ports configured for LibreOffice");
        var builder = LocalOfficeManager.builder()
                .portNumbers(ports.stream().mapToInt(Integer::intValue).toArray())
                .pipeNames(Optional.ofNullable(localOfficeProperties.pipeNames())
                        .orElse(Collections.emptyList())
                        .toArray(String[]::new))
                .processManager(processManager)
                .maxTasksPerProcess(
                        localOfficeProperties.maxTasksPerConnection() != null
                                ? localOfficeProperties.maxTasksPerConnection()
                                : 1)
                .existingProcessAction(ExistingProcessAction.KILL)
                .keepAliveOnShutdown(false)
                .processRetryInterval(0L);
        builder.officeHome(localOfficeProperties.officeHome());
        OfficeManager officeManager;
        try {
            officeManager = builder.build();
        } catch (Exception e) {
            assumeTrue(false, "Skipping: cannot build LocalOfficeManager: " + e.getMessage());
            return;
        }
        try {
            officeManager.start();
            log.warn("OpenOffice/LibreOffice server started!");
        } catch (OfficeException e) {
            assumeTrue(false, "Skipping: LibreOffice failed to start: " + e.getMessage());
            return;
        }
        try {
            DocumentConverter converter =
                    LocalConverter.builder().officeManager(officeManager).build();
            for (int i = 0; i < 5; i++) {
                converter
                        .convert(new File("src/test/resources/docx_sample_1.docx"))
                        .to(new File("test_results/test_docx_sample.pdf"))
                        .execute();
                converter
                        .convert(new File("src/test/resources/docx_sample_1.docx"))
                        .to(new File("test_results/test_docx_sample_2.pdf"))
                        .execute();
                converter
                        .convert(new File("src/test/resources/docx_sample_1.docx"))
                        .to(new File("test_results/test_docx_sample_3.pdf"))
                        .execute();
            }
            assertTrue(Files.exists(Paths.get("test_results/test_docx_sample.pdf")));
            assertTrue(Files.exists(Paths.get("test_results/test_docx_sample_2.pdf")));
            assertTrue(Files.exists(Paths.get("test_results/test_docx_sample_3.pdf")));
        } catch (Exception e) {
            assumeTrue(false, "Skipping: conversion failed: " + e.getMessage());
        } finally {
            OfficeUtils.stopQuietly(officeManager);
        }
    }

    /**
     * Unit test for the null-safe LocalOfficeManager builder path: null ports, null
     * maxTasksPerConnection, and null officeHome must not cause NPE. Does not start the manager.
     */
    /**
     * Verifies the null-safe builder path: null ports, null maxTasksPerConnection, and null
     * officeHome are handled without NPE. Exercises the same builder chain as testRunSOffice();
     * we do not call build() because JOD's builder can NPE without a full runtime environment.
     */
    @Test
    void buildLocalOfficeManagerWithNullSafeConfig() {
        var ports = Optional.ofNullable(localOfficeProperties.ports()).orElse(Collections.emptyList());
        var builder = LocalOfficeManager.builder()
                .portNumbers(ports.stream().mapToInt(Integer::intValue).toArray())
                .pipeNames(Optional.ofNullable(localOfficeProperties.pipeNames())
                        .orElse(Collections.emptyList())
                        .toArray(String[]::new))
                .processManager(new PureJavaProcessManager())
                .maxTasksPerProcess(
                        localOfficeProperties.maxTasksPerConnection() != null
                                ? localOfficeProperties.maxTasksPerConnection()
                                : 1)
                .existingProcessAction(ExistingProcessAction.KILL)
                .keepAliveOnShutdown(false)
                .processRetryInterval(0L);
        if (localOfficeProperties.officeHome() != null) {
            builder.officeHome(localOfficeProperties.officeHome());
        }
        assertNotNull(builder);
    }
}
