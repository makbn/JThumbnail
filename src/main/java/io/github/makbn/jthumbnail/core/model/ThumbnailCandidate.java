package io.github.makbn.jthumbnail.core.model;

import jakarta.validation.constraints.NotNull;
import java.io.File;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * @author Mehdi Akbarian-Rastaghi 2018-10-23
 */
@Data
@RequiredArgsConstructor(staticName = "of", access = AccessLevel.PUBLIC)
public class ThumbnailCandidate {
    @NotNull
    private final File file;

    @NotNull
    private final String uid;

    private String thumbExt;
}
