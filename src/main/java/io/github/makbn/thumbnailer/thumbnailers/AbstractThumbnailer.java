/*
 * regain/Thumbnailer - A file search engine providing plenty of formats (Plugin)
 * Copyright (C) 2011  Come_IN Computerclubs (University of Siegen)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Contact: Come_IN-Team <come_in-team@listserv.uni-siegen.de>
 */

package io.github.makbn.thumbnailer.thumbnailers;

import io.github.makbn.thumbnailer.config.AppSettings;
import io.github.makbn.thumbnailer.exception.ThumbnailerException;

import java.io.File;
import java.io.IOException;

/**
 * This AbstractThumbnailer may be used in order to implement only essential methods.
 * It
 * <li>stores the current thumbnail height/width
 * <li>implements an empty close method
 * <li>specifies an wildcard MIME Type as appropriate Filetype
 *
 * @author Mehdi Akbarian-Rastaghi
 */
public abstract class AbstractThumbnailer implements Thumbnailer {

    /**
     * Height of thumbnail picture to create (in Pixel)
     */
    protected int thumbHeight;

    /**
     *  Width of thumbnail picture to create (in Pixel)
     */
    protected int thumbWidth;

    /**
     * Keep memory if this thumbnailer was closed before.
     */
    protected boolean closed = false;

    /**
     * Initialize the thumbnail size from default constants.
     */
    protected AbstractThumbnailer(AppSettings appSettings) {
        thumbHeight = appSettings.getThumbHeight();
        thumbWidth = appSettings.getThumbWidth();
    }

    /**
     * Get the currently set Image Width of this Thumbnailer.
     *
     * @return image width of created thumbnails.
     */
    public int getCurrentImageWidth() {
        return thumbWidth;
    }

    /**
     * Get the currently set Image Height of this Thumbnailer.
     *
     * @return image height of created thumbnails.
     */
    public int getCurrentImageHeight() {
        return thumbHeight;
    }

    /**
     * This function will be called after all Thumbnails are generated.
     * Note: This acts as a Deconstructor. Do not expect this object to work
     * after calling this method.
     *
     * @throws IOException If some errors occured during finalising
     */
    @Override
    public synchronized void close() throws IOException {
        // Do nothing for now - other Thumbnailer may need cleanup code here.
        closed = true;
    }

    /**
     * Get a list of all MIME Types that this Thumbnailer is ready to process.
     * You should override this method in order to give hints when which Thumbnailer is most appropriate.
     * If you do not override this method, the Thumbnailer will be called in any case - awaiting a ThumbnailException if
     * this thumbnailer cannot treat such a file.
     *
     * @return List of MIME Types. If null, all Files may be passed to this Thumbnailer.
     */
    public String[] getAcceptedMIMETypes() {
        return new String[]{};
    }

    /**
     * Generate a Thumbnail of the input file.
     * (You can override this method if you want to handle the different MIME-Types).
     *
     * @param input    Input file that should be processed
     * @param output   File in which should be written
     * @param mimeType MIME-Type of input file (null if unknown)
     * @throws IOException          If file cannot be read/written
     * @throws ThumbnailerException If the thumbnailing process failed.
     */
    public void generateThumbnail(File input, File output, String mimeType) throws IOException, ThumbnailerException {
        // Ignore MIME-Type-Hint
        generateThumbnail(input, output);
    }
}
