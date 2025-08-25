package io.github.makbn.jthumbnail.core.exception;

/**
 * Thrown if Thumbnailing process failed.
 */
public class ThumbnailException extends Exception {

    public ThumbnailException() {
        super();
    }

    public ThumbnailException(String message) {
        super(message);
    }

    public ThumbnailException(Throwable cause) {
        super(cause);
    }

    public ThumbnailException(String message, Throwable cause) {
        super(message, cause);
    }
}
