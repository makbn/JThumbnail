package io.github.makbn.jthumbnail.core.model;

import java.io.File;

/**
 * @author Mehdi Akbarian-Rastaghi 2018-10-23
 */
public class ThumbnailCandidate {
    private File file;
    private String uid;
    private String thumbExt;

    public ThumbnailCandidate(File file, String uid) {
        this.file = file;
        this.uid = uid;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getThumbExt() {
        return thumbExt;
    }

    public void setThumbExt(String thumbExt) {
        this.thumbExt = thumbExt;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
