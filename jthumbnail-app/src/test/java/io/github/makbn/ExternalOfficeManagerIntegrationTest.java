package io.github.makbn.jthumbnail;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import io.github.makbn.jthumbnail.core.properties.ExternalOfficeProperties;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.ExternalOfficeManager;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;

/**
 * Integration test for external OpenOffice/LibreOffice manager. Requires an external
 * LibreOffice/OpenOffice process listening on the configured ports (e.g. 2002). Disabled
 * by default; enable when running against a real external office instance.
 */
@Log4j2
@SpringBootTest(classes = ExternalOfficeManagerIntegrationTest.class)
@ActiveProfiles("external")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EnableConfigurationProperties(value = {ExternalOfficeProperties.class})
@Disabled("Requires external LibreOffice/OpenOffice on configured ports. Run manually for integration testing.")
class ExternalOfficeManagerIntegrationTest {

    ExternalOfficeProperties externalOfficeProperties;

    @Autowired
    public ExternalOfficeManagerIntegrationTest(ExternalOfficeProperties externalOfficeProperties) {
        this.externalOfficeProperties = externalOfficeProperties;
    }

    @Test
    void testExternalOfficeConversion() throws Exception {
        log.info(
                "Connecting to external Office on host: {}, ports: {}",
                externalOfficeProperties.hostname(),
                externalOfficeProperties.ports());
        OfficeManager officeManager = ExternalOfficeManager.builder()
                .hostName(externalOfficeProperties.hostname())
                .portNumbers(externalOfficeProperties.ports().stream()
                        .mapToInt(Integer::intValue)
                        .toArray())
                .pipeNames(Optional.ofNullable(externalOfficeProperties.pipeNames())
                        .orElse(Collections.emptyList())
                        .toArray(String[]::new))
                .connectOnStart(externalOfficeProperties.connectOnStart())
                .connectFailFast(externalOfficeProperties.failFast())
                .connectTimeout(externalOfficeProperties.connectionTimeout())
                .connectRetryInterval(externalOfficeProperties.connectRetryInterval())
                .maxTasksPerConnection(externalOfficeProperties.maxTasksPerConnection())
                .taskQueueTimeout(externalOfficeProperties.taskQueueTimeout())
                .taskExecutionTimeout(externalOfficeProperties.taskExecutionTimeout())
                .build();

        try {
            officeManager.start();
            log.info("Connected to external OpenOffice/LibreOffice");
        } catch (OfficeException e) {
            fail("Could not connect to external Office: " + e.getMessage());
        }
        try {
            DocumentConverter converter =
                    LocalConverter.builder().officeManager(officeManager).build();
            converter
                    .convert(new File("src/test/resources/docx_sample_1.docx"))
                    .to(new File("test_results/test_external_docx_sample.pdf"))
                    .execute();
        } catch (Exception e) {
            fail(e);
        } finally {
            OfficeUtils.stopQuietly(officeManager);
        }
        assertTrue(Files.exists(Paths.get("test_results/test_external_docx_sample.pdf")));
    }
}
