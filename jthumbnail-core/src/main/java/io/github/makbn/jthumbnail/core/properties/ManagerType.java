package io.github.makbn.jthumbnail.core.properties;

public enum ManagerType {
    LOCAL,
    EXTERNAL,
    REMOTE,
    /** No-op manager for tests or when LibreOffice is not available. */
    NONE
}
