package io.github.makbn.jthumbnail.core.model;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

import java.io.File;

/**
 * Immutable description of a thumbnail job plus optional per-request overrides.
 *
 * <p>The minimal required fields are the input {@link #file} and the caller
 * provided unique {@link #uid}. All additional configuration is optional; when
 * omitted, the engine behaves exactly as it did before configurability was
 * introduced.</p>
 *
 * @author Matt Akbarian (makbn)
 */
@Data
@RequiredArgsConstructor(staticName = "of", access = AccessLevel.PUBLIC)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ThumbnailCandidate {
    @NotNull
    File file;

    @NotNull
    String uid;

    /**
     * Optional per-request configuration. May be {@code null}, in which case
     * default behaviour is used.
     */
    @NonFinal
    ThumbnailConfig config;

    /**
     * Output extension selected for this candidate. This is populated internally
     * by the engine and does not need to be set by callers.
     */
    @NonFinal
    String thumbExt;

    /**
     * Convenience factory for callers who want to provide both UID and config.
     * Defaults are applied by the engine when {@code config} is {@code null}.
     */
    public static ThumbnailCandidate of(File file, String uid, ThumbnailConfig config) {
        ThumbnailCandidate candidate = ThumbnailCandidate.of(file, uid);
        candidate.setConfig(config);
        return candidate;
    }
}

