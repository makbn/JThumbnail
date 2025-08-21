package io.github.makbn.jthumbnail.core.thumbnailers;

import io.github.makbn.jthumbnail.core.config.AppSettings;
import org.jodconverter.core.office.OfficeManager;
import org.springframework.stereotype.Component;

@Component
public class JODExcelThumbnailer extends JODConverterThumbnailer {

    public JODExcelThumbnailer(
            AppSettings settings, OpenOfficeThumbnailer openOfficeThumbnailer, OfficeManager officeManager) {
        super(settings, openOfficeThumbnailer, officeManager);
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
