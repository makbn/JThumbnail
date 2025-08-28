package io.github.makbn.jthumbnail.core.util.mime;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

/**
 * Improve detection of non-XML Office files.
 * <p>
 * Requires:
 * - POI (version 3.7 or higher)
 */
@FieldDefaults(makeFinal = true, level = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class OfficeFileIdentifier implements MimeTypeIdentifier {
    protected static final String PPT_MIME_TYPE = "application/vnd.ms-powerpoint";
    protected static final String XLS_MIME_TYPE = "application/vnd.ms-excel";
    protected static final String DOC_MIME_TYPE = "application/vnd.ms-word";
    protected static final String MS_OFFICE_MIME_TYPE = "application/vnd.ms-office";

    List<String> ext = new ArrayList<>();

    @Override
    public List<String> getExtensionsFor(String mimeType) {
        if (PPT_MIME_TYPE.equals(mimeType)) {
            return ext;
        }
        return new ArrayList<>(); // I don't know
    }

    protected boolean isOfficeFile(String mimeType) {
        if (mimeType == null) return false;

        return MS_OFFICE_MIME_TYPE.equals(mimeType)
                || mimeType.startsWith("application/vnd.ms")
                || mimeType.startsWith("application/vnd.openxmlformats");
    }
}
