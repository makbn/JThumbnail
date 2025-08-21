package io.github.makbn.jthumbnail.api.model;

import java.io.File;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Builder
@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Thumbnail {
    public enum Status {
        GENERATED,
        WAITING,
        FAILED
    }

    String uid;
    Status status;
    File thumbnailFile;
}
