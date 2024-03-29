package io.github.makbn.jthumbnail.core.thumbnailers;

import com.spire.xls.FileFormat;
import com.spire.xls.Workbook;
import io.github.makbn.jthumbnail.core.config.AppSettings;
import io.github.makbn.jthumbnail.core.exception.ThumbnailerException;

import java.io.File;
import java.nio.file.Files;

/**
 * Class for converting Spreadsheet documents into Openoffice-Text files.
 * @deprecated use {@link JODExcelThumbnailer} instead
 * @see JODConverterThumbnailer
 */
@Deprecated(forRemoval = false)
public class ExcelConverterThumbnailer extends AbstractThumbnailer {
    private final OpenOfficeThumbnailer ooThumbnailer;

    //@Autowired
    public ExcelConverterThumbnailer(AppSettings appSettings, OpenOfficeThumbnailer openOfficeThumbnailer) {
        super(appSettings);
        this.ooThumbnailer = openOfficeThumbnailer;
    }

    @Override
    public void generateThumbnail(File input, File output) throws ThumbnailerException {

        //Create a workbook instance
        try {
            Workbook workbook = new Workbook();
            //Load a sample Excel document
            workbook.loadFromFile(input.getAbsolutePath());
            //Fit all worksheets on one page (optional)
            workbook.getConverterSetting().setSheetFitToPage(true);

            File outputTmp = Files.createTempFile("jthumbnailer", "." + "pdf").toFile();

            //Save the workbook to PDF
            workbook.saveToFile(outputTmp.getAbsolutePath(), FileFormat.PDF);
            ooThumbnailer.generateThumbnail(outputTmp, output);
            outputTmp.deleteOnExit();
        } catch (Exception err) {
            throw new ThumbnailerException(err);
        }

    }

    @Override
    public String[] getAcceptedMIMETypes() {
        return new String[]{
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        };
    }

}
