package io.github.makbn.jthumbnail.core.model;

import java.io.File;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

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
