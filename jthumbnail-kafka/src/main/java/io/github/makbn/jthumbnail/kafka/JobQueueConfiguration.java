package io.github.makbn.jthumbnail.kafka;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JobQueueProperties.class)
public class JobQueueConfiguration {}
