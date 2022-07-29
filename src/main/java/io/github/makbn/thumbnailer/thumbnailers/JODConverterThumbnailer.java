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

import io.github.makbn.thumbnailer.AppSettings;
import io.github.makbn.thumbnailer.exception.ThumbnailerException;
import io.github.makbn.thumbnailer.util.IOUtil;
import io.github.makbn.thumbnailer.util.MacProcessManager;
import io.github.makbn.thumbnailer.util.TemporaryFilesManager;
import io.github.makbn.thumbnailer.util.mime.MimeTypeDetector;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.utils.SystemUtils;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.ExistingProcessAction;
import org.jodconverter.local.office.LocalOfficeManager;
import org.jodconverter.local.office.LocalOfficeUtils;

import java.io.File;
import java.io.IOException;


public abstract class JODConverterThumbnailer extends AbstractThumbnailer {

    /**
     * How long may a service work take? (in ms)
     */
    private static final long TIMEOUT = 3000000;
    private static final Logger mLog = LogManager.getLogger("JODConverterThumbnailer");
    /**
     * JOD Office Manager
     */
    protected static OfficeManager officeManager = null;

    private final long counter = 0;
    /**
     * Thumbnail Extractor for OpenOffice Files
     */
    protected OpenOfficeThumbnailer ooo_thumbnailer = null;
    /**
     * MimeIdentification
     */
    protected MimeTypeDetector mimeTypeDetector = null;
    private TemporaryFilesManager temporaryFilesManager = null;

    static {
        initializeOfficeManager(false);
        if (!isConnected())
            try {
                officeManager.start();
                mLog.warn("OpenOffice/LibreOffice server started!");
            } catch (OfficeException e) {
                mLog.warn(e);
            }
    }

    public JODConverterThumbnailer() {
        ooo_thumbnailer = new OpenOfficeThumbnailer();
        mimeTypeDetector = new MimeTypeDetector();
        temporaryFilesManager = new TemporaryFilesManager();
    }

    /**
     * Check if a connection to OpenOffice is established.
     *
     * @return True if connected.
     */
    protected static boolean isConnected() {
        return officeManager != null && officeManager.isRunning();
    }

    /**
     * Start OpenOffice-Service and connect to it.
     *
     * @param forceReconnect Connect even if he is already connected.
     */
    protected static void initializeOfficeManager(boolean forceReconnect) {
        if (!forceReconnect && officeManager != null)
            return;

        if (forceReconnect && isConnected() && officeManager != null) {
            OfficeUtils.stopQuietly(officeManager);
        }

        officeManager = LocalOfficeManager.builder()
                .portNumbers(AppSettings.OPENOFFICE_PORTS)
                .processTimeout(TIMEOUT)
                .processManager(SystemUtils.IS_OS_MAC ? new MacProcessManager() : LocalOfficeUtils.findBestProcessManager())
                .taskExecutionTimeout(TIMEOUT / 10)
                .maxTasksPerProcess(25)
                .existingProcessAction(ExistingProcessAction.KILL)
                .disableOpengl(true)
                .officeHome(AppSettings.OPENOFFICE_PATH)
                .install()
                .build();

        mLog.info("openoffice server initialized!");
    }

    /**
     * Stop the OpenOffice Process and disconnect.
     */
    protected void disconnect() {
        // close the connection
        if (officeManager != null) {
            try {
                officeManager.stop();
            } catch (OfficeException e) {
                mLog.warn("JODConverterThumbnailer", e);
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
     * @throws ThumbnailerException If the creating thumbnail process failed.
     */
    public void generateThumbnail(File input, File output) throws IOException, ThumbnailerException {
        File outputTmp = null;
        try {
            outputTmp = File.createTempFile("jodtemp", "." + getStandardOpenOfficeExtension());

            if (SystemUtils.IS_OS_WINDOWS)
                input = new File(input.getAbsolutePath().replace("\\\\", "\\"));

            try {
                DocumentConverter converter =
                        LocalConverter.builder()
                                .officeManager(officeManager)
                                .build();
                mLog.info("converter created");

                converter.convert(input)
                        .to(outputTmp)
                        .execute();

            } catch (OfficeException e) {
                mLog.warn(e);
                throw new ThumbnailerException(e.getMessage());

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

    @Override
    public String[] getAcceptedMIMETypes() {
        return new String[]{
                "application/vnd.oasis.opendocument.text",
                "application/vnd.oasis.opendocument.text-template",
                "application/vnd.oasis.opendocument.text-web",
                "application/vnd.oasis.opendocument.text-master",
                "application/vnd.oasis.opendocument.graphics",
                "application/vnd.oasis.opendocument.graphics-template",
                "application/vnd.oasis.opendocument.presentation",
                "application/vnd.oasis.opendocument.presentation-template",
                "application/vnd.oasis.opendocument.spreadsheet",
                "application/vnd.oasis.opendocument.spreadsheet-template",
        };
    }
}
