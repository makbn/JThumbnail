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


import io.github.makbn.thumbnailer.ThumbnailerException;
import io.github.makbn.thumbnailer.exception.UnsupportedInputFileFormatException;
import io.github.makbn.thumbnailer.util.ResizeImage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * This class uses Java Image I/O (Java's internal Image Processing library) in order to resize images.
 * JAI can be extended with extra Readers, this Thumbnailer will use all available image readers.
 * <p>
 * Depends:
 * <li>JAI Image I/O Tools (optional, for TIFF support) (@see http://java.net/projects/imageio-ext/ - licence not gpl compatible I suspect ...)
 */
public class NativeImageThumbnailer extends AbstractThumbnailer {

    private static final Logger mLog = LogManager.getLogger("NativeImageThumbnailer");

    public void generateThumbnail(File input, File output) throws IOException, ThumbnailerException {
        ResizeImage resizer = new ResizeImage(thumbWidth, thumbHeight);

        try {
            resizer.setInputImage(input);
        } catch (UnsupportedInputFileFormatException e) {
            mLog.error(e);
            throw new ThumbnailerException("File format could not be interpreted as image", e);
        }
        resizer.writeOutput(output);
    }

    /**
     * Get a List of accepted File Types.
     * Normally, these are: bmp, jpg, wbmp, jpeg, png, gif
     * The exact list may depend on the Java installation.
     *
     * @return MIME-Types
     */
    public String[] getAcceptedMIMETypes() {
        return ImageIO.getReaderMIMETypes();
    }
}