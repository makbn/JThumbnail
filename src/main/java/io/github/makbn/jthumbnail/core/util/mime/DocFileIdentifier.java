package io.github.makbn.jthumbnail.core.util.mime;

import lombok.extern.log4j.Log4j2;
import org.apache.poi.hwpf.HWPFDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Log4j2
public class DocFileIdentifier extends OfficeFileIdentifier {

    public DocFileIdentifier() {
        super();
        ext.add("doc");
    }

    @Override
    public String identify(String mimeType, byte[] bytes, File file) {

        if (isOfficeFile(mimeType) && !DOC_MIME_TYPE.equals(mimeType)) {
            try(FileInputStream stream = new FileInputStream(file);
                HWPFDocument document = new HWPFDocument(stream)) {


                if (document.getRange().getEndOffset() > 0) {
                    return DOC_MIME_TYPE;
                }
            }  catch (IOException | RuntimeException e) {
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
