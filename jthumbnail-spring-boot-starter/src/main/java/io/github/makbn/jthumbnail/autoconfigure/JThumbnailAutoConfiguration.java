package io.github.makbn.jthumbnail.autoconfigure;

import io.github.makbn.jthumbnail.core.JThumbnailer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.ComponentScan;

/**
 * Auto-configuration for embedding JThumbnail core into external Spring Boot applications.
 *
 * <p>When this starter is on the classpath, and {@link JThumbnailer} is present, the core
 * components from {@code io.github.makbn.jthumbnail.core} are automatically registered using
 * component scanning. This follows the standard Spring Boot starter pattern of a dedicated
 * auto-configuration that can be imported via
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}.</p>
 */
@AutoConfiguration
@ConditionalOnClass(JThumbnailer.class)
@ComponentScan(basePackages = "io.github.makbn.jthumbnail.core")
public class JThumbnailAutoConfiguration {}

