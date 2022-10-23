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

import com.spire.doc.Document;
import com.spire.doc.documents.ImageType;
import io.github.makbn.thumbnailer.config.AppSettings;
import io.github.makbn.thumbnailer.exception.ThumbnailerException;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Dummy class for converting Text documents into Openoffice-Textfiles.
 * <p>
 * Tika could be used to detect ms-word-files, but quite a heavy library. Maybe it would be useful as a preperator as well?
 *
 * @see JODConverterThumbnailer
 */
@Component
public class WordConverterThumbnailer extends AbstractThumbnailer {

    @Autowired
    public WordConverterThumbnailer(AppSettings appSettings) {
        super(appSettings);
    }

    @Override
    public void generateThumbnail(File input, File output) throws ThumbnailerException {

        //Create a Document object
        Document doc = new Document();

        //Load a Word document
        doc.loadFromFile(input.getAbsolutePath());

        //Convert the whole document into individual buffered images
        BufferedImage[] pages = doc.saveToImages(ImageType.Bitmap);

        Image image = pages[0].getScaledInstance(thumbWidth, thumbHeight, Image.SCALE_SMOOTH);

        //Re-write the image with a different color space
        BufferedImage newImg = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
        newImg.getGraphics().drawImage(image, 0, 0, null);

        try {
            ImageIO.write(newImg, FilenameUtils.getExtension(output.getName()), output);
        } catch (IOException e) {
            throw new ThumbnailerException(e);
        }
    }

    @Override
    public void generateThumbnail(File input, File output, String mimeType) throws IOException, ThumbnailerException {
        generateThumbnail(input, output);
    }

    @Override
    public String[] getAcceptedMIMETypes() {
        return new String[]{
                "application/vnd.ms-word",
                "application/vnd.openxmlformats-officedocument.wordprocessingml",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/wordperfect",
        };
    }

}
