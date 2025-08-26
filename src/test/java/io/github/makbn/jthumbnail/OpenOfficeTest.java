package io.github.makbn.jthumbnail;

import io.github.makbn.jthumbnail.core.properties.OfficeProperties;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Log4j2
@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:application.yml")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EnableConfigurationProperties(value = {OfficeProperties.class})
class OpenOfficeTest {


    OfficeProperties officeProperties;

    @Autowired
    public OpenOfficeTest(OfficeProperties officeProperties) {
        this.officeProperties = officeProperties;
    }

    @Test
    void testRunSOffice() {
        log.info("SOffice will be running on pipe: {}", officeProperties.pipeNames());
        ProcessManager processManager = new PureJavaProcessManager();
        OfficeManager officeManager = LocalOfficeManager.builder()
                .portNumbers(officeProperties.ports().stream().mapToInt(Integer::intValue).toArray())
                .processManager(processManager)
                .maxTasksPerProcess(officeProperties.maxTasksPerProcess())
                .existingProcessAction(ExistingProcessAction.KILL)
                .officeHome(officeProperties.officeHome())
                .keepAliveOnShutdown(false)
                .processRetryInterval(0L)
                .build();

        try {
            officeManager.start();
            log.warn("OpenOffice/LibreOffice server started!");
        } catch (OfficeException e) {
            fail(e);
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
        } catch (Exception e) {
            fail(e);
        } finally {
            OfficeUtils.stopQuietly(officeManager);
        }
        assertTrue(Files.exists(Paths.get("test_results/test_docx_sample.pdf")));
        assertTrue(Files.exists(Paths.get("test_results/test_docx_sample_2.pdf")));
        assertTrue(Files.exists(Paths.get("test_results/test_docx_sample_3.pdf")));
    }
}
