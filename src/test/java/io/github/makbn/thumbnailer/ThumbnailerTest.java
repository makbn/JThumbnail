package io.github.makbn.thumbnailer;

import io.github.makbn.thumbnailer.listener.ThumbnailListener;
import io.github.makbn.thumbnailer.model.ThumbnailCandidate;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class ThumbnailerTest {

    @ParameterizedTest
    @CsvSource({
            "src/test/resources/xlsx_sample.xlsx, excel_unique_hash_1",
            "src/test/resources/odt_text_sample.odt, odt_text_unique_hash_1",
            "src/test/resources/odp_ppt_sample.odp, odp_ppt_unique_hash_1",
            "src/test/resources/pptx_sample_1.pptx, pptx_unique_hash_1",
            "src/test/resources/docx_sample_1.docx, word_unique_hash_1",
            "src/test/resources/docx_sample_2.docx, word_unique_hash_2",
    })
    void genThumb(String filePath, String uniqueCode) throws InterruptedException {
        String[] args = new String[1];
        AppSettings.init(null);
        File in = new File(filePath);
        CountDownLatch lock = new CountDownLatch(1);
        final File[] output = {null};
        final String[] msg = new String[2];

        if (in.exists()) {
            ThumbnailCandidate candidate = new ThumbnailCandidate(in, uniqueCode);
            JThumbnailer.createThumbnail(candidate, new ThumbnailListener() {
                @Override
                public void onThumbnailReady(String hash, File thumbnail) {
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
