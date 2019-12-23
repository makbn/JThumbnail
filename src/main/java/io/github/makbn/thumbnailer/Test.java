package io.github.makbn.thumbnailer;

import io.github.makbn.thumbnailer.listener.ThumbnailListener;
import io.github.makbn.thumbnailer.model.ThumbnailCandidate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Created by Mehdi Akbarian-Rastaghi on 9/30/18
 */

public class Test {

    private static Logger mLog = LogManager.getLogger("Test");

    public static void main(String[] args) {

        try {
            AppSettings.init(args);
            Thumbnailer.start();
            File in = new File("/home/makbn/Downloads/hhh.txt");
            if(in.exists()) {
                ThumbnailCandidate candidate = new ThumbnailCandidate(in,"unique_code");

                Thumbnailer.createThumbnail(candidate, new ThumbnailListener() {
                    @Override
                    public void onThumbnailReady(String hash, File thumbnail) {
                        mLog.info("FILE created at : " + thumbnail.getAbsolutePath());
                    }

                    @Override
                    public void onThumbnailFailed(String hash, String message, int code) {
                        mLog.warn("Jthumbnail failed!");
                    }
                });
            }
        } catch (IOException e) {
            mLog.error(e);
        }

    }
}
