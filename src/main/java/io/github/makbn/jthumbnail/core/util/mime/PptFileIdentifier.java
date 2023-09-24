package io.github.makbn.jthumbnail.core.util.mime;

import lombok.extern.log4j.Log4j2;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Log4j2
public class PptFileIdentifier extends OfficeFileIdentifier {

    public PptFileIdentifier() {
        super();
        ext.add("ppt");
        ext.add("pps");
    }

    @Override
    public String identify(String mimeType, byte[] bytes, File file) {

        if (isOfficeFile(mimeType) && !PPT_MIME_TYPE.equals(mimeType)) {
            try(FileInputStream stream = new FileInputStream(file); HSLFSlideShow presentation = new HSLFSlideShow(stream)) {
                if (!presentation.getSlides().isEmpty()) {
                    return PPT_MIME_TYPE;
                }
            } catch (IOException | OfficeXmlFileException e) {
                log.debug(e);
            }
        }

        return mimeType;
    }

    @Override
    public String getThumbnailExtension() {
        return "png";
    }

}
