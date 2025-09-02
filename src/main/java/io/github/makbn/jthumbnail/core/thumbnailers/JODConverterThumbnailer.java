package io.github.makbn.jthumbnail.core.thumbnailers;

import io.github.makbn.jthumbnail.core.exception.ThumbnailException;
import io.github.makbn.jthumbnail.core.properties.ThumbnailProperties;
import io.github.makbn.jthumbnail.core.util.IOUtil;
import io.github.makbn.jthumbnail.core.util.mime.MimeTypeDetector;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FilenameUtils;
import org.apache.tika.utils.SystemUtils;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
@Component("jodConverter")
@DependsOn("officeManager")
public abstract class JODConverterThumbnailer extends AbstractThumbnailer {

    /**
     * JOD Office Manager
     */
    protected final OfficeManager officeManager;

    /**
     * Jod converter used
     */
    protected final DocumentConverter converter;

    /**
     * Thumbnail Extractor for OpenOffice Files
     */
    protected final OpenOfficeThumbnailer ooThumbnailer;
    /**
     * MimeIdentification
     */
    protected final MimeTypeDetector mimeTypeDetector;

    protected JODConverterThumbnailer(
            ThumbnailProperties appProperties,
            OpenOfficeThumbnailer openOfficeThumbnailer,
            OfficeManager officeManager,
            DocumentConverter converter) {
        super(appProperties);
        this.ooThumbnailer = openOfficeThumbnailer;
        this.mimeTypeDetector = new MimeTypeDetector();
        this.officeManager = officeManager;
        this.converter = converter;
    }

    /**
     * Stop the OpenOffice Process and disconnect.
     */
    protected void disconnect() {
        // close the connection
        if (officeManager != null && officeManager.isRunning()) {
            try {
                officeManager.stop();
            } catch (OfficeException e) {
                log.error("JODConverterThumbnailer", e);
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
     * @throws ThumbnailException If the creating thumbnail process failed.
     */
    public void generateThumbnail(File input, File output) throws ThumbnailException {
        File outputTmp = null;
        File workingFile = input;
        try {
            outputTmp = Files.createTempFile("jodtemp", "." + getStandardOpenOfficeExtension())
                    .toFile();

            if (SystemUtils.IS_OS_WINDOWS)
                workingFile = new File(workingFile.getAbsolutePath().replace("\\\\", "\\"));

            if (!officeManager.isRunning()) {
                for (int i = 0; i < 10; i++) {
                    Thread.sleep(1000);
                    log.info("waiting for office manager");
                    if (officeManager.isRunning()) break;
                }
            }

            log.info("Using injected converter");
            converter.convert(workingFile).to(outputTmp).execute();

            if (outputTmp.length() == 0) {
                throw new ThumbnailException("Could not convert into OpenOffice-File (file was empty)...");
            }

            ooThumbnailer.generateThumbnail(outputTmp, output);

        } catch (IOException e) {
            throw new ThumbnailException(e);
        } catch (OfficeException e) {
            log.warn(e.getMessage(), e);
            throw new ThumbnailException(e.getMessage());
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
     * @throws IOException        If file cannot be read/written
     * @throws ThumbnailException If the thumbnailing process failed.
     */
    @Override
    public void generateThumbnail(File input, File output, String mimeType) throws IOException, ThumbnailException {
        File workingFile = input;
        String ext = FilenameUtils.getExtension(workingFile.getName());
        if (!mimeTypeDetector.doesExtensionMatchMimeType(ext, mimeType)) {
            String normalizedExtension;

            normalizedExtension = switch (mimeType) {
                case "application/zip" -> getStandardZipExtension();
                case "application/vnd.ms-office" -> getStandardOfficeExtension();
                default -> mimeTypeDetector.getStandardExtensionForMimeType(mimeType);
            };

            workingFile = Files.createTempFile(
                            workingFile.getName(), FilenameUtils.EXTENSION_SEPARATOR + normalizedExtension)
                    .toFile();
        }

        generateThumbnail(workingFile, output);
    }

    protected abstract String getStandardZipExtension();

    protected abstract String getStandardOfficeExtension();

    protected abstract String getStandardOpenOfficeExtension();

    @Override
    public String[] getAcceptedMIMETypes() {
        return new String[] {
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
