package io.github.makbn.jthumbnail.core.thumbnailers;

import io.github.makbn.jthumbnail.core.config.AppSettings;
import org.jodconverter.core.office.OfficeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Dummy class for converting Html documents into Openoffice-Textfiles.
 * <p>
 * (The preview won't be better than what OpenOffice can achieve. See also
 * issue <a href="https://github.com/benjamin4ruby/java-thumbnailer/issues/8">...</a>)
 *
 * @see JODConverterThumbnailer
 */
@Component
public class JODHtmlConverterThumbnailer extends JODConverterThumbnailer {

    @Autowired
    public JODHtmlConverterThumbnailer(AppSettings settings, OpenOfficeThumbnailer openOfficeThumbnailer, OfficeManager manager) {
        super(settings, openOfficeThumbnailer, manager);
    }

    protected String getStandardOpenOfficeExtension() {
        return "odt";
    }

    protected String getStandardZipExtension() {
        return "html";
    }

    protected String getStandardOfficeExtension() {
        return "html";
    }

    @Override
    public String[] getAcceptedMIMETypes() {
        return new String[]{
                "text/html"
        };
    }

}
