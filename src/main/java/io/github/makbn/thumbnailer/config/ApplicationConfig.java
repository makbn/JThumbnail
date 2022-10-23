package io.github.makbn.thumbnailer.config;

import io.github.makbn.thumbnailer.exception.ThumbnailerRuntimeException;
import io.github.makbn.thumbnailer.thumbnailers.JODConverterThumbnailer;
import io.github.makbn.thumbnailer.thumbnailers.JODHtmlConverterThumbnailer;
import io.github.makbn.thumbnailer.thumbnailers.OpenOfficeThumbnailer;
import lombok.extern.log4j.Log4j2;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.office.ExistingProcessAction;
import org.jodconverter.local.office.LocalOfficeManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executor;

@Configuration
@Log4j2
public class ApplicationConfig {

    @Bean
    public AppSettings getSettings() {
        return new AppSettings();
    }

    @Bean("jod_converter")
    public JODConverterThumbnailer getJodConverterThumbnailer(AppSettings settings, OpenOfficeThumbnailer openOfficeThumbnailer, OfficeManager manager) {
        log.debug("jod_converter bean created");
        return new JODHtmlConverterThumbnailer(settings, openOfficeThumbnailer, manager);
    }


    @Bean("office_manager")
    public OfficeManager getOfficeManager(AppSettings settings) throws IOException {
        File dir = Files.createDirectory(Path.of(settings.getOfficeTemporaryDirectory())).toFile();

        OfficeManager officeManager = LocalOfficeManager.builder()
                .portNumbers(settings.getOpenOfficePorts())
                .workingDir(dir)
                .processTimeout(settings.getTimeout())
                .taskExecutionTimeout(settings.getTimeout())
                .maxTasksPerProcess(settings.getMaxTaskPerProcess())
                .existingProcessAction(ExistingProcessAction.CONNECT_OR_KILL)
                .disableOpengl(true)
                .officeHome(settings.getOpenOfficePath())
                .install()
                .build();
        try {
            officeManager.start();
        } catch (OfficeException e) {
            throw new ThumbnailerRuntimeException(e);
        }
        return officeManager;
    }

    @Bean(name = "async_thread_pool_task_executor")
    public Executor threadPoolTaskExecutor(AppSettings settings) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(settings.getAsyncCorePoolSize());
        executor.setMaxPoolSize(settings.getAsyncMaxPoolSize());
        return executor;
    }
}

