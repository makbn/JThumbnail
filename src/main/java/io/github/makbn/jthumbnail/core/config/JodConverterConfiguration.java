package io.github.makbn.jthumbnail.core.config;

import io.github.makbn.jthumbnail.core.properties.OfficeProperties;
import io.github.makbn.jthumbnail.core.properties.ThumbnailProperties;
import io.github.makbn.jthumbnail.core.thumbnailers.JODConverterThumbnailer;
import io.github.makbn.jthumbnail.core.thumbnailers.JODHtmlConverterThumbnailer;
import io.github.makbn.jthumbnail.core.thumbnailers.OpenOfficeThumbnailer;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jodconverter.core.office.OfficeManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class JodConverterConfiguration {

    ThumbnailProperties appProperties;
    OfficeProperties officeProperties;

    @Bean("jodConverter")
    JODConverterThumbnailer getJodConverterThumbnailer(
            OpenOfficeThumbnailer openOfficeThumbnailer, OfficeManager manager) {
        log.debug("jod_converter bean created");
        return new JODHtmlConverterThumbnailer(appProperties, officeProperties, openOfficeThumbnailer, manager);
    }
}
