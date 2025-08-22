package io.github.makbn.jthumbnail.core.config;

import io.github.makbn.jthumbnail.core.properties.AsyncProperties;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ExecutorConfiguration {

    private final AsyncProperties asyncProperties;

    @Bean(name = "asyncThreadPoolTaskExecutor")
    Executor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(asyncProperties.corePoolSize());
        executor.setMaxPoolSize(asyncProperties.maxPoolSize());
        return executor;
    }
}
