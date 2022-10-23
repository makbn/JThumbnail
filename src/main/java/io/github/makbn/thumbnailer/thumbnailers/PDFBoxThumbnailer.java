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
import io.github.makbn.thumbnailer.exception.ThumbnailerRuntimeException;
import io.github.makbn.thumbnailer.util.ResizeImage;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Renders the first page of a PDF file into a thumbnail.
 */
@Component
public class PDFBoxThumbnailer extends AbstractThumbnailer {
    @Autowired
    public PDFBoxThumbnailer(AppSettings appSettings) {
        super(appSettings);
    }
    private PDDocument getDocument(File input) throws IOException {
        return PDDocument.load(input);
    }
    @Override
    public void generateThumbnail(File input, File output) throws ThumbnailerException, ThumbnailerRuntimeException {
        if(!Files.exists(input.toPath())){
            throw new ThumbnailerException("input file does not exist");
        }

        if (input.length() == 0)
            throw new ThumbnailerException("File is empty");
        FileUtils.deleteQuietly(output);

    try( PDDocument document = getDocument(input)) {
        BufferedImage tmpImage = writeImageFirstPage(document);

        if (tmpImage.getWidth() == thumbWidth) {
            ImageIO.write(tmpImage, "PNG", output);
        } else {
            ResizeImage resizer = new ResizeImage(thumbWidth, thumbHeight);
            resizer.setResizeMethod(ResizeImage.RESIZE_FIT_BOTH_DIMENSIONS);
            resizer.setInputImage(tmpImage);
            resizer.writeOutput(output);
        }
        } catch (IllegalArgumentException e) {
            throw new ThumbnailerRuntimeException(e.getMessage());
        } catch (IOException e) {
            throw new ThumbnailerException(e);
        }
    }

    /**
     * Loosely based on the commandline-Tool PDFImageWriter
     *
     * @param document to generate image from first page
     * @return generated image
     */
    private BufferedImage writeImageFirstPage(PDDocument document) throws IOException {
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        return pdfRenderer.renderImageWithDPI(0, 72, ImageType.RGB);
    }

    /**
     * Get a List of accepted File Types.
     * Only PDF Files are accepted.
     *
     * @return MIME-Types
     */
    @Override
    public String[] getAcceptedMIMETypes() {
        return new String[]{
                "application/pdf"
        };
    }


}
