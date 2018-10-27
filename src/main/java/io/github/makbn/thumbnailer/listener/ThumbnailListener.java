package io.github.makbn.thumbnailer.listener;

import java.io.File;

public interface ThumbnailListener {

    void onThumbnailReady(String hash, File thumbnail);

    void onThumbnailFailed(String hash, String message, int code);
}
