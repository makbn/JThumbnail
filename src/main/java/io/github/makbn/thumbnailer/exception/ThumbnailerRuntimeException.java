package io.github.makbn.thumbnailer.exception;

public class ThumbnailerRuntimeException extends RuntimeException {

    public ThumbnailerRuntimeException(String message) {
        super(message);
    }

    public ThumbnailerRuntimeException(Throwable cause) {
        super(cause);
    }
}
