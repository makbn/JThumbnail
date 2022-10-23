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
import io.github.makbn.thumbnailer.util.IOUtil;
import io.github.makbn.thumbnailer.util.mime.MimeTypeDetector;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.utils.SystemUtils;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component("jod_converter")
@DependsOn("office_manager")
@Log4j2
public abstract class JODConverterThumbnailer extends AbstractThumbnailer {

    /**
     * JOD Office Manager
     */
    protected final OfficeManager officeManager;

    /**
     * Thumbnail Extractor for OpenOffice Files
     */
    protected final OpenOfficeThumbnailer ooThumbnailer;
    /**
     * MimeIdentification
     */
    protected final MimeTypeDetector mimeTypeDetector;

    private final String officeDir;

    @Autowired
    protected JODConverterThumbnailer(AppSettings settings, OpenOfficeThumbnailer openOfficeThumbnailer, OfficeManager officeManager) {
        super(settings);
        this.officeDir = settings.getOfficeTemporaryDirectory();
        this.ooThumbnailer = openOfficeThumbnailer;
        this.mimeTypeDetector = new MimeTypeDetector();
        this.officeManager = officeManager;
    }

    /**
     * Stop the OpenOffice Process and disconnect.
     */
    protected void disconnect() {
        // close the connection
        if (officeManager != null && officeManager.isRunning()) {
            try {
                IOUtil.deleteQuietlyForce(Path.of(officeDir).toFile());
                officeManager.stop();
            } catch (OfficeException e) {
                log.warn("JODConverterThumbnailer", e);
            }
        }
    }

    @Override
    public synchronized void close() throws IOException {
        super.close();
        disconnect();
        ooThumbnailer.close();
    }

    /**
     * Generates a thumbnail of Office files.
     *
     * @param input  Input file that should be processed
     * @param output File in which should be written
     * @throws ThumbnailerException If the creating thumbnail process failed.
     */
    public void generateThumbnail(File input, File output) throws ThumbnailerException {
        File outputTmp = null;
        try {
            outputTmp = File.createTempFile("jodtemp", "." + getStandardOpenOfficeExtension());

            if (SystemUtils.IS_OS_WINDOWS)
                input = new File(input.getAbsolutePath().replace("\\\\", "\\"));

            if(!officeManager.isRunning()){
                for (int i=0; i<10;i++){
                    Thread.sleep(1000);
                    log.info("waiting for office manager");
                    if (officeManager.isRunning())
                        break;
                }
            }
            DocumentConverter converter =
                    LocalConverter.builder()
                            .officeManager(officeManager)
                            .build();
            log.info("converter created");

            converter.convert(input)
                    .to(outputTmp)
                    .execute();



            if (outputTmp.length() == 0) {
                throw new ThumbnailerException("Could not convert into OpenOffice-File (file was empty)...");
            }

            ooThumbnailer.generateThumbnail(outputTmp, output);

        } catch (IOException e) {
            throw new ThumbnailerException(e);
        } catch (OfficeException e) {
            log.warn(e);
            throw new ThumbnailerException(e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
    @Override
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

            input = Files.createTempFile(input.getName(), newExt).toFile();
        }

        generateThumbnail(input, output);
    }

    protected abstract String getStandardZipExtension();

    protected abstract String getStandardOfficeExtension();

    protected abstract String getStandardOpenOfficeExtension();

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
