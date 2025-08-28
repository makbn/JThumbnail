package io.github.makbn.jthumbnail.core.thumbnailers;

import io.github.makbn.jthumbnail.core.properties.ThumbnailProperties;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeManager;
import org.springframework.stereotype.Component;

@Component
public class JODExcelThumbnailer extends JODConverterThumbnailer {

    public JODExcelThumbnailer(
            ThumbnailProperties appProperties,
            OpenOfficeThumbnailer openOfficeThumbnailer,
            OfficeManager officeManager,
            DocumentConverter converter) {
        super(appProperties, openOfficeThumbnailer, officeManager, converter);
    }

    protected String getStandardOpenOfficeExtension() {
        return "ods";
    }

    protected String getStandardZipExtension() {
        return "xlsx";
    }

    protected String getStandardOfficeExtension() {
        return "xls";
    }

    @Override
    public String[] getAcceptedMIMETypes() {
        return new String[] {
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        };
    }
}
