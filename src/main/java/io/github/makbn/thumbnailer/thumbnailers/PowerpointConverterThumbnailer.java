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

import com.spire.presentation.Presentation;
import io.github.makbn.thumbnailer.config.AppSettings;
import io.github.makbn.thumbnailer.exception.ThumbnailerException;
import io.github.makbn.thumbnailer.exception.ThumbnailerRuntimeException;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Dummy class for converting Presentation documents into Openoffice-Textfiles.
 *
 * @see JODConverterThumbnailer
 */
@Component
public class PowerpointConverterThumbnailer extends AbstractThumbnailer {

    @Autowired
    public PowerpointConverterThumbnailer(AppSettings appSettings) {
        super(appSettings);
    }

    @Override
    public void generateThumbnail(File input, File output) throws ThumbnailerException {
        Presentation ppt = new Presentation();
        try {
            ppt.loadFromFile(input.getAbsolutePath());
            //Save PPT document to images
            Image image = ppt.getSlides().get(0).saveAsImage().getScaledInstance(thumbWidth, thumbHeight, Image.SCALE_SMOOTH);
            //Re-write the image with a different color space
            BufferedImage newImg = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
            newImg.getGraphics().drawImage(image, 0, 0, null);
            ImageIO.write(newImg, FilenameUtils.getExtension(output.getName()), output);

        } catch (Exception e) {
            throw new ThumbnailerRuntimeException(e);
        } finally {
            ppt.dispose();
        }

    }

    @Override
    public void generateThumbnail(File input, File output, String mimeType) throws ThumbnailerException {
        generateThumbnail(input, output);
    }

    /**
     * Get a List of accepted File Types.
     * All Presentation Office Formats that OpenOffice understands are accepted.
     * (ppt, pptx, pps, ppsx)
     *
     * @return MIME-Types
     * @see <a href="http://www.artofsolving.com/opensource/jodconverter/guide/supportedformats">...</a>
     */
    @Override
    public String[] getAcceptedMIMETypes() {
        return new String[]{
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",

        };
    }

}
