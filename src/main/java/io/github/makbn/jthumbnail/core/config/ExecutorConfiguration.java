package io.github.makbn.jthumbnail.core.config;

import io.github.makbn.jthumbnail.core.properties.AsyncProperties;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Slf4j
@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ExecutorConfiguration {
    static class ContextAwareForkJoinWorkerThread extends Thread {
        public ContextAwareForkJoinWorkerThread(Runnable target) {
            super(target);
            setName("jthumbnail-worker-" + threadId());
            setContextClassLoader(Thread.currentThread().getContextClassLoader());
        }
    }

    AsyncProperties asyncProperties;

    @Bean(name = "asyncThreadPoolTaskExecutor")
    Executor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadFactory(ContextAwareForkJoinWorkerThread::new);
        executor.setCorePoolSize(asyncProperties.corePoolSize());
        executor.setMaxPoolSize(asyncProperties.maxPoolSize());
        return executor;
    }
}
