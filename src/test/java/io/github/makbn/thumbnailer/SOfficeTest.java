package io.github.makbn.thumbnailer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.ExistingProcessAction;
import org.jodconverter.local.office.LocalOfficeManager;
import org.jodconverter.local.process.MacProcessManager;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class SOfficeTest {
    private static final Logger mLog = LogManager.getLogger("SOfficeTest");

    @ParameterizedTest
    @ValueSource(ints = {2002})
    void run_soffice(int port) throws OfficeException, IOException {
        mLog.info(String.format("SOffice will be running on port: %d", port));
        //Files.deleteIfExists(Path.of("test_results/test_docx_sample.pdf"));
        OfficeManager officeManager = LocalOfficeManager.builder()
                .portNumbers(port)
                .processManager(new MacProcessManager())
                .maxTasksPerProcess(1)
                .existingProcessAction(ExistingProcessAction.CONNECT)
                //.officeHome("/Applications/LibreOffice.app/Contents/")
                .build();

        try {
            officeManager.start();
            mLog.warn("OpenOffice/LibreOffice server started!");
        } catch (OfficeException e) {
            fail(e);
        }
        try {
            for (int i = 0; i < 5; i++) {
                DocumentConverter converter =
                        LocalConverter.builder()
                                .officeManager(officeManager)
                                .build();

                converter.convert(new File("src/test/resources/docx_sample_1.docx")).to(new File("test_results/test_docx_sample.pdf")).execute();
                converter.convert(new File("src/test/resources/docx_sample_1.docx")).to(new File("test_results/test_docx_sample_2.pdf")).execute();
                converter.convert(new File("src/test/resources/docx_sample_1.docx")).to(new File("test_results/test_docx_sample_3.pdf")).execute();


            }
        } catch (Exception e) {
            fail(e);
        } finally {
            OfficeUtils.stopQuietly(officeManager);
        }
        assertTrue(Files.exists(Paths.get("test_results/test_docx_sample.pdf")));

    }
}
