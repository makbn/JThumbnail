package io.github.makbn.jthumbnail.core.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

import java.io.File;

/**
 * @author Mehdi Akbarian-Rastaghi 2018-10-23
 */
@Data
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor(staticName = "of", access = AccessLevel.PUBLIC)
public class ThumbnailCandidate {
    @NonNull
    File file;

    @NonNull
    String uid;

    @NonFinal
    String thumbExt;
}
