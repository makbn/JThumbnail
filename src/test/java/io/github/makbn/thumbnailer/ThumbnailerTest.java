package io.github.makbn.thumbnailer;

import io.github.makbn.thumbnailer.listener.ThumbnailListener;
import io.github.makbn.thumbnailer.model.ThumbnailCandidate;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@Log4j2
class ThumbnailerTest {

    private static JThumbnailer jThumbnailer;

    @BeforeAll
    public static void init(){
        log.info("Starting jThumbnailer ...");
        String[] args = new String[]{};
        jThumbnailer = JThumbnailerStarter.init(args);
    }

    @AfterAll
    public static void destroy(){
        jThumbnailer.close();
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
            ThumbnailCandidate candidate = new ThumbnailCandidate(in, uniqueCode);
            jThumbnailer.run(candidate, new ThumbnailListener() {
                @Override
                public void onThumbnailReady(String hash, File thumbnail) {
                    try {
                        Files.copy(thumbnail.toPath(), Path.of("test_results", thumbnail.getName()), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        fail(String.format("message: %s",e.getMessage()));
                    }
                    output[0] = thumbnail;
                    msg[0] = hash;
                    lock.countDown();
                }

                @Override
                public void onThumbnailFailed(String hash, String message, int code) {
                    msg[0] = hash;
                    msg[1] = String.valueOf(message);
                    lock.countDown();
                }
            });

            boolean executed = lock.await(500, TimeUnit.SECONDS);
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


}
