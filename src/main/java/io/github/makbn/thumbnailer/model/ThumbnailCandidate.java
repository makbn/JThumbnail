package io.github.makbn.thumbnailer.model;

import java.io.File;

/**
 * @author Mehdi Akbarian-Rastaghi 2018-10-23
 */
public class ThumbnailCandidate {
    private File file;
    private String hash;
    private String thumbExt;
    private String outputName;

    public ThumbnailCandidate(File file, String hash, String thumbExt) {
        this.file = file;
        this.hash = hash;
        this.thumbExt = thumbExt;
    }

    public ThumbnailCandidate(File file, String hash, String thumbExt, String outputName) {
        this.file = file;
        this.hash = hash;
        this.thumbExt = thumbExt;
        this.outputName = outputName;
    }

    public ThumbnailCandidate(File file, String hash) {
        this.file = file;
        this.hash = hash;
    }

    public String getOutputName() {
        return outputName;
    }

    public void setOutputName(String outputName) {
        this.outputName = outputName;
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
