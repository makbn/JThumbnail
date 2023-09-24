package io.github.makbn.jthumbnail.core.util.mime;

import lombok.extern.log4j.Log4j2;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Log4j2
public class XlsFileIdentifier extends OfficeFileIdentifier {

    public XlsFileIdentifier() {
        super();
        ext.add("xls");
    }

    @Override
    public String identify(String mimeType, byte[] bytes, File file) {

        if (isOfficeFile(mimeType) && !XLS_MIME_TYPE.equals(mimeType)) {
            try(FileInputStream stream = new FileInputStream(file); HSSFWorkbook workbook = new HSSFWorkbook(stream)) {
                if (workbook.getNumberOfSheets() != 0) {
                    return XLS_MIME_TYPE;
                }
            }  catch (IOException | OfficeXmlFileException e) {
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
