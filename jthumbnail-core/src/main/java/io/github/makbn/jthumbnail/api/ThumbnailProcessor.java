package io.github.makbn.jthumbnail.api;

import java.io.File;

/**
 * Shared abstraction for synchronous thumbnail generation. Implementations may delegate to the core
 * library or other backends. Used by the Kafka sink task and can be used by the webservice or other
 * packages.
 */
public interface ThumbnailProcessor {

    /**
     * Generate a thumbnail for the given input file.
     *
     * @param input the source file to create a thumbnail for
     * @return the generated thumbnail file, or null if generation failed
     */
    File createThumbnail(File input);
}
