package io.github.makbn.jthumbnail;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import io.github.makbn.jthumbnail.core.JThumbnailer;
import io.github.makbn.jthumbnail.core.listener.ThumbnailListener;
import io.github.makbn.jthumbnail.core.model.ThumbnailCandidate;
import lombok.extern.log4j.Log4j2;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@Log4j2
class ThumbnailerTest {

    private static JThumbnailer jThumbnailer;

    @BeforeAll
    static void init() throws IOException {
        log.info("Starting jThumbnailer ...");
        String[] args = new String[] {};
        Files.createDirectories(Path.of("test_results"));
        jThumbnailer = JThumbnailerStarter.init(args);
    }

    @AfterAll
    static void destroy() {
        try {
            jThumbnailer.close();
        } catch (Exception e) {
            System.exit(0);
        }
    }

    @ParameterizedTest
    @CsvSource({
        "src/test/resources/xlsx_sample.xlsx, excel_unique_hash_1",
        "src/test/resources/odt_text_sample.odt, odt_text_unique_hash_1",
        "src/test/resources/odp_ppt_sample.odp, odp_ppt_unique_hash_1",
        "src/test/resources/pptx_sample_1.pptx, pptx_unique_hash_1",
        "src/test/resources/docx_sample_1.docx, word_unique_hash_1",
        "src/test/resources/docx_sample_2.docx, word_unique_hash_2",
        "src/test/resources/xlsx_sample.xlsx, excel_unique_hash_2",
        "src/test/resources/txt_sample_1.txt, txt_unique_hash_1",
    })
    void genThumb(String filePath, String uniqueCode) throws InterruptedException {

        File in = new File(filePath);
        CountDownLatch lock = new CountDownLatch(1);
        final File[] output = {null};
        final String[] msg = new String[2];

        if (in.exists()) {
            ThumbnailCandidate candidate = ThumbnailCandidate.of(in, uniqueCode);
            jThumbnailer.run(candidate, new ThumbnailListener() {
                @Override
                public void onThumbnailReady(String hash, File thumbnail) {
                    try {
                        Files.copy(
                                thumbnail.toPath(),
                                Path.of("test_results", thumbnail.getName()),
                                StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        fail(String.format("message: %s", e.getMessage()));
                    } finally {
                        output[0] = thumbnail;
                        msg[0] = hash;
                        lock.countDown();
                    }
                }

                @Override
                public void onThumbnailFailed(String hash, String message, int code) {
                    try {
                        msg[0] = hash;
                        msg[1] = String.valueOf(message);
                    } finally {
                        lock.countDown();
                    }
                }
            });

            boolean executed = lock.await(60, TimeUnit.SECONDS);
            if (executed) {
                if (output[0] != null) {
                    assertTrue(Files.exists(output[0].toPath()), "FILE created at : " + output[0].getAbsolutePath());
                } else {
                    fail(String.format("code: %s\tmessage: %s", msg[0], msg[1]));
                }
            } else {
                fail("process timeout");
            }
        }
    }

    @Test
    void createApplicationModuleModel() {
        ApplicationModules modules = ApplicationModules.of(JThumbnailerStarter.class);
        modules.forEach(System.out::println);
        Assertions.assertEquals(2, modules.stream().count());
    }

    @Test
    void verifiesModularStructure() {
        ApplicationModules modules = ApplicationModules.of(JThumbnailerStarter.class);
        modules.verify();
    }
}
