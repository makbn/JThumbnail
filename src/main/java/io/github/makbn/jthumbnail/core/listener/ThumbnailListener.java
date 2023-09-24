package io.github.makbn.jthumbnail.core.listener;

import java.io.File;

public interface ThumbnailListener {
    void onThumbnailReady(String hash, File thumbnail);

    void onThumbnailFailed(String hash, String message, int code);
}
