package io.github.makbn.jthumbnail.core.thumbnailers;

import io.github.makbn.jthumbnail.core.exception.ThumbnailerException;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 * This interface is implemented by any method suitable to create a thumbnail of a given File.
 *
 * @author Benjamin
 */
public interface Thumbnailer extends Closeable {

    /**
     * Generate a Thumbnail of the input file.
     *
     * @param input    Input file that should be processed
     * @param output   File in which should be written
     * @param mimeType MIME-Type of input file (null if unknown)
     * @throws IOException          If file cannot be read/written
     * @throws ThumbnailerException If the thumbnailing process failed.
     */
    void generateThumbnail(File input, File output, String mimeType) throws IOException, ThumbnailerException;

    /**
     * Generate a Thumbnail of the input file.
     *
     * @param input  Input file that should be processed
     * @param output File in which should be written
     * @throws IOException          If file cannot be read/written
     * @throws ThumbnailerException If the thumbnailing process failed.
     */
    void generateThumbnail(File input, File output) throws IOException, ThumbnailerException;

    /**
     * This function will be called after all Thumbnails are generated.
     * Note: This acts as a Deconstruct. Do not expect this object to work
     * after calling this method.
     *
     * @throws IOException If some errors occurred during finalising
     */
    void close() throws IOException;

    /**
     * Get the currently set Image Width of this Thumbnailer.
     *
     * @return image width of created thumbnails.
     */
    int getCurrentImageWidth();

    /**
     * Get the currently set Image Height of this Thumbnailer.
     *
     * @return image height of created thumbnails.
     */
    int getCurrentImageHeight();

    /**
     * Get a list of all MIME Types that this Thumbnailer is ready to process.
     *
     * @return Array of MIME Types. If null, all Files may be passed to this Thumbnailer.
     */
    String[] getAcceptedMIMETypes();
}
