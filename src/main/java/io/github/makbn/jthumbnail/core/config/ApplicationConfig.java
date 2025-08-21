package io.github.makbn.jthumbnail.core.config;

import io.github.makbn.jthumbnail.core.thumbnailers.JODConverterThumbnailer;
import io.github.makbn.jthumbnail.core.thumbnailers.JODHtmlConverterThumbnailer;
import io.github.makbn.jthumbnail.core.thumbnailers.OpenOfficeThumbnailer;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.jodconverter.core.office.OfficeManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration("jtApplicationConfig")
@Slf4j
public class ApplicationConfig {

    @Bean
    public AppSettings getSettings() {
        return new AppSettings();
    }

    @Bean("jodConverter")
    public JODConverterThumbnailer getJodConverterThumbnailer(
            AppSettings settings, OpenOfficeThumbnailer openOfficeThumbnailer, OfficeManager manager) {
        log.debug("jod_converter bean created");
        return new JODHtmlConverterThumbnailer(settings, openOfficeThumbnailer, manager);
    }

    @Bean(name = "asyncThreadPoolTaskExecutor")
    public Executor threadPoolTaskExecutor(AppSettings settings) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(settings.getAsyncCorePoolSize());
        executor.setMaxPoolSize(settings.getAsyncMaxPoolSize());
        return executor;
    }
}
