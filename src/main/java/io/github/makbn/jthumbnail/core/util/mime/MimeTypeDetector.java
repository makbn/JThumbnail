package io.github.makbn.jthumbnail.core.util.mime;

import io.github.makbn.jthumbnail.core.exception.ThumbnailException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;

/**
 * Wrapper class for MIME Identification of Files.
 */
@Slf4j
public class MimeTypeDetector {
    private static final Map<String, String> outputThumbnailExtensionCache = new HashMap<>();
    private final List<MimeTypeIdentifier> extraIdentifiers;
    private final Map<String, List<String>> extensionsCache = new HashMap<>();

    /**
     * Create a MimeType Detector and init it.
     */
    public MimeTypeDetector() {
        extraIdentifiers = new ArrayList<>();

        addMimeTypeIdentifier(new Office2007FileIdentifier());
        addMimeTypeIdentifier(new PptFileIdentifier());
        addMimeTypeIdentifier(new XlsFileIdentifier());
        addMimeTypeIdentifier(new DocFileIdentifier());
        addMimeTypeIdentifier(new MP3FileIdentifier());
        addMimeTypeIdentifier(new MPEGFileIdentifier());

        if (outputThumbnailExtensionCache.isEmpty()) {
            for (MimeTypeIdentifier identifier : extraIdentifiers) {
                List<String> exts = identifier.getExtensionsFor(null);
                if (exts != null)
                    exts.forEach(ext -> outputThumbnailExtensionCache.put(ext, identifier.getThumbnailExtension()));
            }
        }
    }

    /**
     * Add a new MimeTypeIdentifier to this Detector.
     * MimeTypeIdentifier may override the decision of the detector.
     * The order the identifiers are added will also be the order they will be executed
     * (i.e., the last identifiers may override all others.)
     *
     * @param identifier a new MimeTypeIdentifier
     */
    public void addMimeTypeIdentifier(MimeTypeIdentifier identifier) {
        extraIdentifiers.add(identifier);
    }

    /**
     * Detect MIME-Type for this file.
     *
     * @param file File to analyse
     * @return String of MIME-Type, or null if no detection was possible (or unknown MIME Type)
     */
    public String getMimeType(File file) throws IOException {

        String mimeType = Files.probeContentType(file.toPath());

        if (mimeType == null || mimeType.isEmpty()) {
            Tika tika = new Tika();
            mimeType = tika.detect(file);
        }

        try {
            if (mimeType == null || mimeType.isEmpty())
                mimeType = file.toURI().toURL().openConnection().getContentType();

        } catch (Exception e) {
            log.debug(e.getMessage());
        }

        if (mimeType != null && mimeType.isEmpty()) mimeType = null;

        // Identifiers may re-write MIME.
        for (MimeTypeIdentifier identifier : extraIdentifiers) mimeType = identifier.identify(mimeType, null, file);

        log.info("Detected MIME-Type of {} is {}", file.getName(), mimeType);
        return mimeType;
    }

    /**
     * Return the standard extension of a specific MIME-Type.
     * What are these files "normally" called?
     *
     * @param mimeType MIME-Type, e.g. "text/plain"
     * @return Extension, e.g. "txt"
     */
    public String getStandardExtensionForMimeType(String mimeType) {
        List<String> extensions = getExtensionsCached(mimeType);

        if (extensions == null) return null;

        try {
            return extensions.getFirst();
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    protected List<String> getExtensionsCached(String mimeType) {
        List<String> extensions = extensionsCache.get(mimeType);
        if (extensions != null) return extensions;

        extensions = new ArrayList<>();
        switch (mimeType) {
            case "application/vnd.openxmlformats-officedocument.wordprocessingml" -> {
                extensions.add("docx");
                extensions.add("dotx");
            }
            case "application/vnd.openxmlformats-officedocument.presentationml" -> {
                extensions.add("pptx");
                extensions.add("sldx");
                extensions.add("ppsx");
                extensions.add("potx");
            }
            case "application/vnd.openxmlformats-officedocument.spreadsheetml" -> {
                extensions.add("xlsx");
                extensions.add("xltx");
            }
            case "application/vnd.ms-powerpoint" -> {
                extensions.add("ppt");
                extensions.add("ppam");
                extensions.add("sldm");
                extensions.add("pptm");
                extensions.add("ppsm");
                extensions.add("potm");
            }
            case "application/msword" -> {
                extensions.add("doc");
                extensions.add("docm");
                extensions.add("dotm");
            }
            case "application/pdf" -> extensions.add("pdf");
            default -> log.warn("no ext found!");
        }
        extensionsCache.put(mimeType, extensions);
        return extensions;
    }

    /**
     * Test if a given extension can contain a File of MIME-Type
     *
     * @param extension Filename extension (e.g. "txt")
     * @param mimeType  MIME-Type		   (e.g. "text/plain")
     * @return True if compatible.
     */
    public boolean doesExtensionMatchMimeType(String extension, String mimeType) {
        List<String> extensions;
        extensions = getExtensionsCached(mimeType);
        if (extensions == null) return false;

        return extensions.contains(extension);
    }

    /**
     * get output file extension for the current input file!
     * after first time extension cached for next requests!
     *
     * @param file input file for generating thumbnail.
     * @return the identified extension or 'png' in other cases
     * @throws ThumbnailException if there is a problem on reading file
     */
    public String getOutputExt(File file) throws ThumbnailException {
        try {
            String ext = FilenameUtils.getExtension(file.getName());
            String mime = getMimeType(file);
            if (outputThumbnailExtensionCache.containsKey(ext)) return outputThumbnailExtensionCache.get(ext);

            for (MimeTypeIdentifier identifier : extraIdentifiers) {
                List<String> exts = identifier.getExtensionsFor(mime);
                if (exts != null && exts.contains(ext)) {
                    String result = identifier.getThumbnailExtension();
                    outputThumbnailExtensionCache.put(ext, result);
                    return result;
                }
            }
        } catch (IOException e) {
            throw new ThumbnailException(e);
        }

        return "png";
    }
}
