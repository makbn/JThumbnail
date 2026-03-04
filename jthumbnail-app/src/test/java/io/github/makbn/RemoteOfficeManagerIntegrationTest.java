package io.github.makbn.jthumbnail;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import io.github.makbn.jthumbnail.core.properties.RemoteOfficeProperties;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.remote.RemoteConverter;
import org.jodconverter.remote.office.RemoteOfficeManager;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Integration test for remote OpenOffice/LibreOffice manager (e.g. Collabora CODE). Requires
 * a remote office REST API (e.g. podman run -p 9980:9980 -e "extra_params=--o:ssl.enable=false"
 * docker.io/collabora/code). Disabled by default; enable when running against a real remote server.
 */
@Log4j2
@SpringBootTest(classes = RemoteOfficeManagerIntegrationTest.class)
@ActiveProfiles("remote")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EnableConfigurationProperties(value = {RemoteOfficeProperties.class})
@Disabled("Requires remote Office REST API (e.g. Collabora CODE on port 9980). Run manually for integration testing.")
class RemoteOfficeManagerIntegrationTest {

    RemoteOfficeProperties remoteOfficeProperties;

    @Autowired
    public RemoteOfficeManagerIntegrationTest(RemoteOfficeProperties remoteOfficeProperties) {
        this.remoteOfficeProperties = remoteOfficeProperties;
    }

    @Test
    void testRemoteOfficeConversion() throws Exception {
        log.info("Connecting to remote Office at: {}", remoteOfficeProperties.urlConnection());
        OfficeManager officeManager = RemoteOfficeManager.builder()
                .urlConnection(remoteOfficeProperties.urlConnection().toString())
                .connectTimeout(remoteOfficeProperties.connectionTimeout())
                .poolSize(remoteOfficeProperties.poolSize())
                .socketTimeout(remoteOfficeProperties.socketTimeout())
                .taskExecutionTimeout(remoteOfficeProperties.taskExecutionTimeout())
                .taskQueueTimeout(remoteOfficeProperties.taskQueueTimeout())
                .install()
                .build();

        try {
            officeManager.start();
            log.info("Connected to remote OpenOffice/LibreOffice");
        } catch (OfficeException e) {
            fail("Could not connect to remote Office: " + e.getMessage());
        }
        try {
            DocumentConverter converter =
                    RemoteConverter.builder().officeManager(officeManager).build();
            converter
                    .convert(new File("src/test/resources/docx_sample_1.docx"))
                    .to(new File("test_results/test_remote_docx_sample.pdf"))
                    .execute();
        } catch (Exception e) {
            fail(e);
        } finally {
            OfficeUtils.stopQuietly(officeManager);
        }
        assertTrue(Files.exists(Paths.get("test_results/test_remote_docx_sample.pdf")));
    }
}
