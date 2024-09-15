package io.github.makbn.jthumbnail.core.util.mime;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * created by Mehdi Akbarian-Rastaghi 2018-10-23
 */
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class MPEGFileIdentifier implements MimeTypeIdentifier {
    List<String> ext = new ArrayList<>();

    public MPEGFileIdentifier() {
        ext.add("mp4");
        ext.add("mkv");
        ext.add("ts");
        ext.add("3gp");
        ext.add("avi");
    }

    @Override
    public String identify(String mimeType, byte[] bytes, File file) {
        return mimeType;
    }

    @Override
    public List<String> getExtensionsFor(String mimeType) {
        return ext;
    }

    @Override
    public String getThumbnailExtension() {
        return "gif";
    }
}
