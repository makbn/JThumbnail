package io.github.makbn.thumbnailer.model;

import java.io.File;

/**
 * @author Mehdi Akbarian-Rastaghi 2018-10-23
 */
public class ThumbnailCandidate {
    private File file;
    private String hash;
    private String thumbExt;

    public ThumbnailCandidate(File file, String hash) {
        this.file = file;
        this.hash = hash;
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

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
