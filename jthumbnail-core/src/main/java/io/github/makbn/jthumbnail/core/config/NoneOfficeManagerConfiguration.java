package io.github.makbn.jthumbnail.core.config;

import io.github.makbn.jthumbnail.core.office.NoOpDocumentConverter;
import io.github.makbn.jthumbnail.core.office.NoOpOfficeManager;
import lombok.extern.slf4j.Slf4j;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnProperty(value = "jthumbnailer.openoffice.manager_type", havingValue = "none")
public class NoneOfficeManagerConfiguration {

    @Bean("officeManager")
    OfficeManager officeManager() {
        log.info("Using NoOp OfficeManager (LibreOffice not configured)");
        return new NoOpOfficeManager();
    }

    @Bean("converter")
    DocumentConverter converter() {
        return new NoOpDocumentConverter();
    }
}
