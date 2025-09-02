package io.github.makbn.jthumbnail.core.thumbnailers;

import io.github.makbn.jthumbnail.core.properties.ThumbnailProperties;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeManager;
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

    public JODHtmlConverterThumbnailer(
            ThumbnailProperties appProperties,
            OpenOfficeThumbnailer openOfficeThumbnailer,
            OfficeManager manager,
            DocumentConverter converter) {
        super(appProperties, openOfficeThumbnailer, manager, converter);
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
        return new String[] {"text/html"};
    }
}
