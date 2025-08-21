package io.github.makbn.jthumbnail.core.exception;

/**
 * Thrown if Thumbnailing process failed.
 */
public class ThumbnailerException extends Exception {

    public ThumbnailerException() {
        super();
    }

    public ThumbnailerException(String message) {
        super(message);
    }

    public ThumbnailerException(Throwable cause) {
        super(cause);
    }

    public ThumbnailerException(String message, Throwable cause) {
        super(message, cause);
    }
}
