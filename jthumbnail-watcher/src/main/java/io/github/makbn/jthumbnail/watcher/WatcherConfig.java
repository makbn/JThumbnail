package io.github.makbn.jthumbnail.watcher;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Binds {@link WatcherProperties} when watcher is enabled. */
@Configuration
@ConditionalOnProperty(name = "jthumbnailer.watcher.enabled", havingValue = "true")
@EnableConfigurationProperties(WatcherProperties.class)
public class WatcherConfig {}
