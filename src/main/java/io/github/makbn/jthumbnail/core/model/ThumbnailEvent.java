package io.github.makbn.jthumbnail.core.model;

import java.io.File;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Builder
@Getter
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ThumbnailEvent {

    public enum Status {
        GENERATED,
        WAITING,
        FAILED
    }

    String uid;
    Status status;
    File thumbnailFile;
}
