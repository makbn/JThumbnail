package io.github.makbn.jthumbnail.core.config;

import io.github.makbn.jthumbnail.core.util.mime.MimeTypeDetector;
import io.github.makbn.jthumbnail.core.util.mime.MimeTypeIdentifier;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;

/**
 * Provides a shared {@link MimeTypeDetector} bean so that custom
 * {@link MimeTypeIdentifier} beans can be used for custom file types.
 * Any bean of type {@link MimeTypeIdentifier} is automatically added to the detector.
 */
@Configuration
@Slf4j
public class MimeTypeDetectorConfiguration {

    @Bean
    public MimeTypeDetector mimeTypeDetector(Optional<List<MimeTypeIdentifier>> customIdentifiers) {
        MimeTypeDetector detector = new MimeTypeDetector();
        customIdentifiers.orElse(List.of()).forEach(id -> {
            detector.addMimeTypeIdentifier(id);
            log.debug(
                    "Registered custom MIME type identifier: {}", id.getClass().getSimpleName());
        });
        return detector;
    }
}
