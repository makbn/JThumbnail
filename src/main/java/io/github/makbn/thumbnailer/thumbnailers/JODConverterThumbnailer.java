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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;


import io.github.makbn.thumbnailer.ThumbnailerException;
import io.github.makbn.thumbnailer.util.mime.MimeTypeDetector;
import io.github.makbn.thumbnailer.util.IOUtil;
import io.github.makbn.thumbnailer.util.Platform;
import io.github.makbn.thumbnailer.util.TemporaryFilesManager;
import org.jodconverter.LocalConverter;
import org.jodconverter.OfficeDocumentConverter;
import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.office.LocalOfficeManager;
import org.jodconverter.office.OfficeContext;
import org.jodconverter.office.OfficeException;
import org.jodconverter.office.OfficeManager;
import org.jodconverter.process.ProcessManager;
import org.jodconverter.task.OfficeTask;

/**
 * This generates a thumbnail of Office-Files by converting them into the OpenOffice-Format first.
 * <p>
 * Performance note: this may take several seconds per file. The first time this Thumbnailer is called, OpenOffice is started.
 * <p>
 * Depends on:
 * <li>OpenOffice 3 / LibreOffice
 * <li>JODConverter 3 beta5 or higher
 * <li>Aperture Core (MIME Type detection)
 * <li>OpenOfficeThumbnailer
 *
 * @author Benjamin
 * @TODO Be stricter about which kind of files to process. Currently it converts just like everything. (See tests/ThumbnailersFailingTest)
 */
public abstract class JODConverterThumbnailer extends AbstractThumbnailer {


    /**
     * JOD Office Manager
     */
    protected static OfficeManager officeManager = null;

    /**
     * JOD Converter
     */
    protected static OfficeDocumentConverter officeConverter = null;

    /**
     * Thumbnail Extractor for OpenOffice Files
     */
    protected OpenOfficeThumbnailer ooo_thumbnailer = null;

    /**
     * MimeIdentification
     */
    protected MimeTypeDetector mimeTypeDetector = null;

    private TemporaryFilesManager temporaryFilesManager = null;

    /**
     * The Port on which to connect (must be unoccupied)
     */
    private final static int OOO_DEFAULT_PORT = 9852;

    /**
     * How long may a service work take? (in ms)
     */
    private static final long TIMEOUT = 3600000;


    public JODConverterThumbnailer() {
        ooo_thumbnailer = new OpenOfficeThumbnailer();
        mimeTypeDetector = new MimeTypeDetector();
        temporaryFilesManager = new TemporaryFilesManager();
    }


    /**
     * Start OpenOffice-Service and connect to it.
     * (Does not reconnect if already connected.)
     */
    public void connect() {
        connect(false);
    }

    /**
     * Start OpenOffice-Service and connect to it.
     *
     * @param forceReconnect Connect even if he is already connected.
     */
    public void connect(boolean forceReconnect) {
        if (!forceReconnect && isConnected())
            return;

        officeManager = LocalOfficeManager.builder()
                .portNumbers(OOO_DEFAULT_PORT,OOO_DEFAULT_PORT+1,OOO_DEFAULT_PORT+2,OOO_DEFAULT_PORT+3)
                .processTimeout(TIMEOUT)
                .maxTasksPerProcess(200)
                .killExistingProcess(true)
                .officeHome("/opt/openoffice4/")
                .build();

        try {
            officeManager.start();
        } catch (OfficeException e) {
            e.printStackTrace();
        }
        officeConverter = new OfficeDocumentConverter(officeManager);
    }

    /**
     * Check if a connection to OpenOffice is established.
     *
     * @return True if connected.
     */
    public static boolean isConnected() {
        return officeManager != null && officeManager.isRunning();
    }

    /**
     * Stop the OpenOffice Process and disconnect.
     */
    public static void disconnect() {
        // close the connection
        if (officeManager != null) {
            try {
                officeManager.stop();
            } catch (OfficeException e) {
                e.printStackTrace();
            }
        }
        officeManager = null;
    }

    public void close() throws IOException {
        try {
            try {
                temporaryFilesManager.deleteAllTempfiles();
                ooo_thumbnailer.close();
            } finally {
                disconnect();
            }
        } finally {
            super.close();
        }
    }


    /**
     * Generates a thumbnail of Office files.
     *
     * @param input  Input file that should be processed
     * @param output File in which should be written
     * @throws IOException          If file cannot be read/written
     * @throws ThumbnailerException If the thumbnailing process failed.
     */
    public void generateThumbnail(File input, File output) throws IOException, ThumbnailerException {
        // Connect on first use
        if (!isConnected())
            connect();

        File outputTmp = null;
        try {
            outputTmp = File.createTempFile("jodtemp", "." + getStandardOpenOfficeExtension());

            // Naughty hack to circumvent invalid URLs under windows (C:\\ ...)
            if (Platform.isWindows())
                input = new File(input.getAbsolutePath().replace("\\\\", "\\"));

            /*officeConverter.convert(new FileInputStream(input))
                    .as(DefaultDocumentFormatRegistry.getFormatByExtension(
                    FilenameUtils.getExtension(input.getAbsolutePath()))).to(outputTmp);*/

            try {
                officeConverter.convert(input,outputTmp);
            } catch (OfficeException e) {
                e.printStackTrace();
            }
            if (outputTmp.length() == 0) {
                throw new ThumbnailerException("Could not convert into OpenOffice-File (file was empty)...");
            }

            ooo_thumbnailer.generateThumbnail(outputTmp, output);
        } finally {
            IOUtil.deleteQuietlyForce(outputTmp);
        }
    }

    /**
     * Generate a Thumbnail of the input file.
     * (Fix file ending according to MIME-Type).
     *
     * @param input    Input file that should be processed
     * @param output   File in which should be written
     * @param mimeType MIME-Type of input file (null if unknown)
     * @throws IOException          If file cannot be read/written
     * @throws ThumbnailerException If the thumbnailing process failed.
     */
    public void generateThumbnail(File input, File output, String mimeType) throws IOException, ThumbnailerException {
        String ext = FilenameUtils.getExtension(input.getName());
        if (!mimeTypeDetector.doesExtensionMatchMimeType(ext, mimeType)) {
            String newExt;
            if ("application/zip".equals(mimeType))
                newExt = getStandardZipExtension();
            else if ("application/vnd.ms-office".equals(mimeType))
                newExt = getStandardOfficeExtension();
            else
                newExt = mimeTypeDetector.getStandardExtensionForMimeType(mimeType);

            input = temporaryFilesManager.createTempfileCopy(input, newExt);
        }

        generateThumbnail(input, output);
    }

    protected abstract String getStandardZipExtension();

    protected abstract String getStandardOfficeExtension();

    protected abstract String getStandardOpenOfficeExtension();

    public void setImageSize(int thumbWidth, int thumbHeight, int imageResizeOptions) {
        super.setImageSize(thumbWidth, thumbHeight, imageResizeOptions);
        ooo_thumbnailer.setImageSize(thumbWidth, thumbHeight, imageResizeOptions);
    }
}
