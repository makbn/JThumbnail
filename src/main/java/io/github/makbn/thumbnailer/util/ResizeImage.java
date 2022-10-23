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

package io.github.makbn.thumbnailer.util;


import io.github.makbn.thumbnailer.exception.UnsupportedInputFileFormatException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;


public class ResizeImage {

    /**
     * Scale input image so that width and height is equal (or smaller) to the output size.
     * The other dimension will be smaller or equal than the output size.
     */
    public static final int RESIZE_FIT_BOTH_DIMENSIONS = 2;
    /**
     * Scale input image so that width or height is equal to the output size.
     * The other dimension will be bigger or equal than the output size.
     */
    public static final int RESIZE_FIT_ONE_DIMENSION = 3;

    /**
     * Do not try to scale the image up, only down. If bigger, center it.
     */
    public static final int DO_NOT_SCALE_UP = 16;
    /**
     * If output image is bigger than input image, allow the output to be smaller than expected (the size of the input image)
     */
    public static final int ALLOW_SMALLER = 32;
    private static final Logger mLog = LogManager.getLogger("ResizeImage");
    private static final int EXTRA_OPTIONS = DO_NOT_SCALE_UP;
    private int resizeMethod = RESIZE_FIT_ONE_DIMENSION;
    private BufferedImage inputImage;
    private boolean isProcessed = false;
    private BufferedImage outputImage;
    private int imageWidth;
    private int imageHeight;
    private int thumbWidth;
    private int thumbHeight;
    private int scaledWidth;
    private int scaledHeight;
    private int offsetX;
    private int offsetY;


    public ResizeImage(int thumbWidth, int thumbHeight) {
        this.thumbWidth = thumbWidth;
        this.thumbHeight = thumbHeight;
    }

    public void setInputImage(File input) throws IOException {
        BufferedImage image = ImageIO.read(input);
        setInputImage(image);
    }

    public void setInputImage(InputStream input) throws IOException {
        BufferedImage image = ImageIO.read(input);
        setInputImage(image);
    }

    public void setInputImage(BufferedImage input) throws UnsupportedInputFileFormatException {
        if (input == null)
            throw new UnsupportedInputFileFormatException("The image reader could not open the file.");

        this.inputImage = input;
        isProcessed = false;
        imageWidth = inputImage.getWidth(null);
        imageHeight = inputImage.getHeight(null);
    }

    public void writeOutput(File output) throws IOException {
        writeOutput(output, "PNG");
    }

    public void writeOutput(File output, String format) throws IOException {
        if (!isProcessed)
            process();

        ImageIO.write(outputImage, format, output);
    }

    private void process() {
        if (imageWidth == thumbWidth && imageHeight == thumbHeight)
            outputImage = inputImage;
        else {
            calcDimensions(resizeMethod);
            paint();
        }

        isProcessed = true;
    }

    private void calcDimensions(int resizeMethod) {

        double resizeRatio = switch (resizeMethod) {
            case RESIZE_FIT_BOTH_DIMENSIONS ->
                    Math.min(((double) thumbWidth) / imageWidth, ((double) thumbHeight) / imageHeight);
            case RESIZE_FIT_ONE_DIMENSION ->
                    Math.max(((double) thumbWidth) / imageWidth, ((double) thumbHeight) / imageHeight);
            default -> 1.0;
        };

        if ((EXTRA_OPTIONS & DO_NOT_SCALE_UP) > 0 && resizeRatio > 1.0)
            resizeRatio = 1.0;


        scaledWidth = (int) Math.round(imageWidth * resizeRatio);
        scaledHeight = (int) Math.round(imageHeight * resizeRatio);

        if ((EXTRA_OPTIONS & ALLOW_SMALLER) > 0 && scaledWidth < thumbWidth && scaledHeight < thumbHeight) {
            thumbWidth = scaledWidth;
            thumbHeight = scaledHeight;
        }

        // Center if smaller.
        if (scaledWidth < thumbWidth)
            offsetX = (thumbWidth - scaledWidth) / 2;
        else
            offsetX = 0;

        if (scaledHeight < thumbHeight)
            offsetY = (thumbHeight - scaledHeight) / 2;
        else
            offsetY = 0;
    }

    private void paint() {
        outputImage = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics2D = outputImage.createGraphics();

        // Fill background with white color
        graphics2D.setBackground(Color.WHITE);
        graphics2D.setPaint(Color.WHITE);
        graphics2D.fillRect(0, 0, thumbWidth, thumbHeight);

        // Enable smooth, high-quality resampling
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);


        CompletableFuture<Boolean> isImageReady = new CompletableFuture<>();
        boolean scalingComplete = graphics2D.drawImage(inputImage, offsetX, offsetY, scaledWidth, scaledHeight, (img, flags, x, y, width, height)-> {
            isImageReady.complete(true);
            return true;
        });

        if (!scalingComplete) {
            mLog.debug("ResizeImage: Scaling is not yet complete!");
            CompletableFuture.allOf(isImageReady).join();
        }

        graphics2D.dispose();
    }

    public void setResizeMethod(int resizeFitBothDimensions) {
        resizeMethod = resizeFitBothDimensions;
    }
}
