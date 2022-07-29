package io.github.makbn.thumbnailer.listener;

import io.github.makbn.thumbnailer.exception.ThumbnailerRuntimeException;

public interface JTConsumerCallback {
    void onException(ThumbnailerRuntimeException e);
}
