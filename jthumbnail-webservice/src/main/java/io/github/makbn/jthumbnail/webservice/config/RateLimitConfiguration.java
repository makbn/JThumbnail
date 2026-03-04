package io.github.makbn.jthumbnail.webservice.config;

import io.github.makbn.jthumbnail.core.config.RateLimitProperties;
import io.github.makbn.jthumbnail.webservice.filter.RateLimitFilter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registers {@link RateLimitFilter} and binds {@link RateLimitProperties}. */
@Configuration
@ConditionalOnWebApplication
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfiguration {

    @Bean
    public RateLimitFilter rateLimitFilter(RateLimitProperties properties) {
        return new RateLimitFilter(properties);
    }
}
