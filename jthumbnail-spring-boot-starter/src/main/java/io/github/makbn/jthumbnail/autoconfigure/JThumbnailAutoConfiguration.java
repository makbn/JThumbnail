package io.github.makbn.jthumbnail.autoconfigure;

import io.github.makbn.jthumbnail.core.JThumbnailer;
import io.github.makbn.jthumbnail.core.properties.AsyncProperties;
import io.github.makbn.jthumbnail.core.properties.FfmpegProperties;
import io.github.makbn.jthumbnail.core.properties.ThumbnailProperties;
import io.github.makbn.jthumbnail.core.properties.ThumbnailServerProperties;
import io.github.makbn.jthumbnail.core.provider.ProviderRegistryProperties;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

/**
 * Auto-configuration for embedding JThumbnail core into external Spring Boot applications.
 *
 * <h2>How it starts</h2>
 * <p>This does <em>not</em> start a separate application. When your Spring Boot context
 * starts, this configuration runs and registers all JThumbnail core beans into your
 * context via component scanning of {@code io.github.makbn.jthumbnail.core}. You get
 * {@link JThumbnailer}, {@link io.github.makbn.jthumbnail.core.ThumbnailerManager}, job
 * services, thumbnailers, and conditional beans (e.g. Office manager, async executor)
 * as regular Spring beans. Inject {@link JThumbnailer} and call
 * {@link JThumbnailer#run(io.github.makbn.jthumbnail.core.model.ThumbnailCandidate)
 * run(ThumbnailCandidate)} to generate thumbnails.</p>
 *
 * <h2>Configuration from your application properties</h2>
 * <p>All config is bound from your {@code application.properties} / {@code application.yml}
 * under the {@code jthumbnailer.*} namespace. This class explicitly enables the following
 * property types so they are bound and validated at startup:</p>
 * <ul>
 *   <li>{@code jthumbnailer.thumbnail} – default thumbnail size (thumb-width, thumb-height)</li>
 *   <li>{@code jthumbnailer.server} – upload directory and max waiting list size</li>
 *   <li>{@code jthumbnailer.async} – thread pool (core-pool-size, max-pool-size)</li>
 *   <li>{@code jthumbnailer.providers} – provider priority order</li>
 *   <li>{@code jthumbnailer.ffmpeg} – FFmpeg paths and frame options</li>
 *   <li>{@code jthumbnailer.openoffice} – Office manager type and connection (local / remote / external / none)</li>
 * </ul>
 * <p>Office-related beans (e.g. {@code officeManager}, {@code converter}) are created
 * only when {@code jthumbnailer.openoffice.manager-type} matches (local, remote, external, or none).</p>
 *
 * <h2>Disabling auto-configuration</h2>
 * <p>Set {@code jthumbnailer.enabled=false} in your properties to disable this
 * auto-configuration and not register any JThumbnail beans.</p>
 *
 * <p>Imported via
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}.</p>
 */
@AutoConfiguration
@ConditionalOnClass(JThumbnailer.class)
@ConditionalOnProperty(name = "jthumbnailer.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({
    ThumbnailProperties.class,
    ThumbnailServerProperties.class,
    AsyncProperties.class,
    ProviderRegistryProperties.class,
    FfmpegProperties.class,
})
@ComponentScan(basePackages = "io.github.makbn.jthumbnail.core")
public class JThumbnailAutoConfiguration {}
