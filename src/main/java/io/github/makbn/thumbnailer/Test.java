package io.github.makbn.thumbnailer;

import io.github.makbn.thumbnailer.listener.ThumbnailListener;
import io.github.makbn.thumbnailer.model.ThumbnailCandidate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * @author Mehdi Akbarian Rastaghi on 9/30/18
 */

public class Test {

    private final static Logger mLog = LogManager.getLogger(Test.class);

    public static void main(String[] args) {

        try {
            AppSettings.init(args);
            Thumbnailer.start();
            var in = new File("PATH_TO_A_DOCUMENT");
            if (in.exists()) {
                var candidate = new ThumbnailCandidate(in, "unique_code");
                Thumbnailer.createThumbnail(candidate, new ThumbnailListener() {
                    @Override
                    public void onThumbnailReady(String hash, File thumbnail) {
                        System.out.println("FILE created at : " + thumbnail.getAbsolutePath());
                    }

                    @Override
                    public void onThumbnailFailed(String hash, String message, int code) {
                        System.err.printf("Message:%s\tCode:%d%n", message, code);
                    }
                });
            }
        } catch (IOException e) {
            mLog.error(e);
        }

    }
}
