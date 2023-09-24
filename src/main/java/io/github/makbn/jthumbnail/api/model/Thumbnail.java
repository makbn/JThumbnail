package io.github.makbn.jthumbnail.api.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.io.File;

@Builder
@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Thumbnail {
    public enum Status {
        GENERATED, WAITING, FAILED
    }

    String uid;
    Status status;
    File thumbnailFile;
}
