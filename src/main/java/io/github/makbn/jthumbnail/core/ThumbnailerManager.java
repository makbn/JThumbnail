package io.github.makbn.jthumbnail.core;

import io.github.makbn.jthumbnail.core.exception.ThumbnailerException;
import io.github.makbn.jthumbnail.core.exception.ThumbnailerRuntimeException;
import io.github.makbn.jthumbnail.core.model.ExecutionResult;
import io.github.makbn.jthumbnail.core.thumbnailers.Thumbnailer;
import io.github.makbn.jthumbnail.core.util.mime.MimeTypeDetector;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * This class manages all available Thumbnailers.
 * Its purpose is to delegate a File to the appropriate Thumbnailer in order to get a Thumbnail of it.
 * This is done in a fall-through manner: If several Thumbnailer can handle a specific filetype,
 * all are tried until a Thumbnail could be created.
 * <p>
 * Fill this class with available Thumbnailers via the registerThumbnailer()-Method.
 * Then call generateThumbnail().
 *
 * @author Benjamin
 */
@Component
@DependsOn({"DWGThumbnailer", "JODExcelThumbnailer", "PDFBoxThumbnailer", "MPEGThumbnailer",
        "openOfficeThumbnailer", "jodConverter", "MP3Thumbnailer", "powerpointConverterThumbnailer",
        "JODHtmlConverterThumbnailer", "nativeImageThumbnailer", "textThumbnailer", "imageThumbnailer", "wordConverterThumbnailer"})
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ThumbnailerManager implements Thumbnailer {

    /**
     * MIME Type for "all MIME" within thumbnailers Hash
     */
    private static final String ALL_MIME_WILDCARD = "*/*";

    /**
     * Magic Mime Detection ... a wrapper class to Aperature's Mime thingies.
     */
    MimeTypeDetector mimeTypeDetector;
    /**
     * Thumbnailers per MIME-Type they accept (ALL_MIME_WILDCARD for all)
     */
    Map<String, List<Thumbnailer>> thumbnailers;

    /**
     * Folder under which new thumbnails should be filed
     */
    @NonFinal
    File thumbnailFolder;

    /**
     * Initialise Thumbnail Manager
     */
    @Autowired
    public ThumbnailerManager(List<? extends Thumbnailer> thumbnailers) {
        this.thumbnailers = registerThumbnailer(thumbnailers);
        this.mimeTypeDetector = new MimeTypeDetector();
    }

    /**
     * Calculate a thumbnail filename (via hashing).
     *
     * @param input Input file
     * @return The chosen filename
     */
    public File chooseThumbnailFilename(File input, String ext) throws ThumbnailerException {
        if (thumbnailFolder == null) {
            try {
                thumbnailFolder = Files.createTempDirectory("jthumbnailer").toFile();
            } catch (IOException e) {
                throw new ThumbnailerException(e);
            }
        }
        if (input == null)
            throw new IllegalArgumentException("Input file may not be null");

        return new File(thumbnailFolder, String.format("%s%s.%s", FilenameUtils.getBaseName(input.getName()), "_thumb", ext));
    }

    /**
     * Generate a Thumbnail.
     * The output file name is generated using a hashing scheme.
     * It is guaranteed that an existing Thumbnail is not overwritten by this.
     *
     * @param input Input file that should be processed.
     * @return Name of Thumbnail-File generated.
     * @throws ThumbnailerException        in case something known related to the thumbnail generation process happens
     * @throws ThumbnailerRuntimeException in case something unknown related to the thumbnail generation process happens
     */
    public File createThumbnail(File input, String ext) throws ThumbnailerRuntimeException, ThumbnailerException {
        File output = chooseThumbnailFilename(input, ext);
        generateThumbnail(input, output);

        return output;
    }

    /**
     * Add a {@link Thumbnailer} to the list of available Thumbnailers
     * Note that the order you add Thumbnailers may make a difference:
     * First added Thumbnailers are tried first, if one fails, the next
     * (that claims to be able to treat such a document) is tried.
     * (Thumbnailers that claim to treat all MIME Types are tried last, though.)
     *
     * @param thumbnailers list of {@link Thumbnailer} to add.
     */
    public Map<String, List<Thumbnailer>> registerThumbnailer(List<? extends Thumbnailer> thumbnailers) {
        HashMap<String, List<Thumbnailer>> chainedHashMap = new HashMap<>();

        thumbnailers.stream().map(th -> Map.entry(List.of(th.getAcceptedMIMETypes()), th))
                .forEach(entry -> entry.getKey().forEach(mime -> {
                    if (chainedHashMap.containsKey(mime))
                        chainedHashMap.get(mime).add(entry.getValue());
                    else {
                        ArrayList<Thumbnailer> thList = new ArrayList<>();
                        thList.add(entry.getValue());
                        chainedHashMap.put(mime, thList);
                    }
                }));

        return chainedHashMap;
    }

    /**
     * Instead of a deconstructor:
     * De-initialize ThumbnailManager and its thumbnailers.
     * <p>
     * This functions should be called before termination of the program,
     * and Thumbnails can't be generated after calling this function.
     */
    public synchronized void close() {
        thumbnailers.values()
                .stream()
                .filter(Predicate.not(ThumbnailerManager.class::isInstance))
                .flatMap(List::stream)
                .collect(Collectors.toSet()).forEach(th -> {
                    try {
                        log.info("closing {} ...", th.getClass().getSimpleName());
                        th.close();
                    } catch (IOException e) {
                        log.error("error during close of thumbnailer:", e);
                    }
                });

        thumbnailers.clear();
    }

    /**
     * Generate a Thumbnail of the input file.
     * Try all available Thumbnailers and use the first that returns an image.
     * <p>
     * MIME-Detection narrows the selection of Thumbnailers to try:
     * <li>First all Thumbnailers that declare to accept such a MIME Type are used
     * <li>Then all Thumbnailers that declare to accept all possible MIME Types.
     *
     * @param input    Input file that should be processed
     * @param output   File in which should be written
     * @param mimeType MIME-Type of input file (null if unknown)
     * @throws ThumbnailerException If the thumbnailing process failed
     *                              (i.e., no thumbnailer could generate an Thumbnail.
     *                              The last ThumbnailerException is re-thrown.)
     */
    @Override
    public void generateThumbnail(File input, File output, String mimeType) throws ThumbnailerRuntimeException, ThumbnailerException {
        if (!Files.exists(input.toPath())) {
            throw new ThumbnailerException("the input file does not exist");
        }

        mimeType = getMIMEType(input, mimeType);

        if (mimeType != null) {
            ExecutionResult generated = executeThumbnailers(mimeType, input, output, mimeType);
            // Try again using wildcard thumbnailers
            if (!generated.isGenerated() && !generated.hasException())
                generated = executeThumbnailers(ALL_MIME_WILDCARD, input, output, mimeType);

            if (!generated.isGenerated()) {
                Throwable exp = generated.getException();
                if (exp instanceof ThumbnailerException thumbnailerException)
                    throw thumbnailerException;
                else if (exp instanceof ThumbnailerRuntimeException thumbnailerRuntimeException)
                    throw thumbnailerRuntimeException;
                else if (exp instanceof RuntimeException runtimeException)
                    throw runtimeException;
                else
                    throw new ThumbnailerException(exp);
            }
        } else {
            throw new ThumbnailerException(String.format("Jthumbnailer failed on identifying the MIME type for: %s", input.getName()));
        }
    }

    private String getMIMEType(File input, String mimeType) throws ThumbnailerException {
        String result = mimeType;
        try {
            if (result == null) {
                result = mimeTypeDetector.getMimeType(input);
                log.debug("Detected MIME type: {}", result);
            }
        } catch (IOException e) {
            throw new ThumbnailerException(e);
        }
        return result;
    }

    /**
     * Generate a Thumbnail of the input file.
     * Try all available Thumbnailers and use the first that returns an image.
     * <p>
     * MIME-Detection narrows the selection of Thumbnailers to try:
     * <li>First all Thumbnailers that declare to accept such a MIME Type are used
     * <li>Then all Thumbnailers that declare to accept all possible MIME Types.
     *
     * @param input  Input file that should be processed
     * @param output File in which should be written
     * @throws ThumbnailerException If the thumbnailing process failed
     *                              (i.e., no thumbnailer could generate an Thumbnail.
     *                              The last ThumbnailerException is re-thrown.)
     */
    public void generateThumbnail(File input, File output) throws ThumbnailerRuntimeException, ThumbnailerException {
        generateThumbnail(input, output, null);
    }

    /**
     * Helper function for Thumbnail generation:
     * execute all thumbnailers of a given MimeType.
     *
     * @param useMimeType      Which MIME Type the thumbnailers should be taken from
     * @param input            Input File that should be processed
     * @param output           Output file where the image shall be written.
     * @param detectedMimeType MIME Type that was returned by automatic MIME Detection
     * @return True on success (1 thumbnailer could generate the output file).
     */
    private ExecutionResult executeThumbnailers(String useMimeType, File input, File output, String detectedMimeType) {
        ExecutionResult result = ExecutionResult.failed(new ThumbnailerException("No suitable Thumbnailer has been " +
                "found for: " + input.getName()));

        for (Thumbnailer thumbnailer : thumbnailers.get(useMimeType)) {
            try {
                if (thumbnailer instanceof ThumbnailerManager)
                    continue;
                thumbnailer.generateThumbnail(input, output, detectedMimeType);
                result = ExecutionResult.success();
                return result;
            } catch (ThumbnailerRuntimeException e) {
                log.warn("pass runtime error to Thumbnailer");
                result = ExecutionResult.failed(e);
            } catch (ThumbnailerException | IOException e) {
                // This Thumbnailer apparently wasn't suitable, so try next
                result = ExecutionResult.failed(e);
            } catch (Exception e) {
                log.error("unknown exception occurred!");
                result = ExecutionResult.failed(e);
            }
        }
        return result;
    }

    /**
     * Get the currently set Image Width of this Thumbnailer.
     *
     * @return image width of created thumbnails.
     */
    @Override
    public int getCurrentImageWidth() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the currently set Image Height of this Thumbnailer.
     *
     * @return image height of created thumbnails.
     */
    @Override
    public int getCurrentImageHeight() {
        throw new UnsupportedOperationException();
    }


    /**
     * Summarize all contained MIME Type Thumbnailers.
     *
     * @return All accepted MIME Types, null if any.
     */
    @Override
    public String[] getAcceptedMIMETypes() {
        throw new UnsupportedOperationException("getting accepted MIME types not allowed");
    }

}
