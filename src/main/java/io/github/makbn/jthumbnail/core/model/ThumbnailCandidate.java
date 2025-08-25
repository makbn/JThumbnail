package io.github.makbn.jthumbnail.core.model;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

import java.io.File;

/**
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

    @NonFinal
    String thumbExt;
}
