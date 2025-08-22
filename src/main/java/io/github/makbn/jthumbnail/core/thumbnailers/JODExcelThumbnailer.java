package io.github.makbn.jthumbnail.core.thumbnailers;

import io.github.makbn.jthumbnail.core.properties.ThumbnailProperties;
import io.github.makbn.jthumbnail.core.properties.OfficeProperties;
import org.jodconverter.core.office.OfficeManager;
import org.springframework.stereotype.Component;

@Component
public class JODExcelThumbnailer extends JODConverterThumbnailer {

    public JODExcelThumbnailer(
            ThumbnailProperties appProperties,
            OfficeProperties officeProperties,
            OpenOfficeThumbnailer openOfficeThumbnailer,
            OfficeManager officeManager) {
        super(appProperties, officeProperties, openOfficeThumbnailer, officeManager);
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
