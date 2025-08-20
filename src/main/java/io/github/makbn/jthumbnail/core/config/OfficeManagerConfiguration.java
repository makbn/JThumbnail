package io.github.makbn.jthumbnail.core.config;

import java.io.File;
import java.io.IOException;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.office.ExistingProcessAction;
import org.jodconverter.local.office.LocalOfficeManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import io.github.makbn.jthumbnail.core.exception.ThumbnailerRuntimeException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Configuration("jtOfficeManagerConfiguration")
@DependsOn("jtApplicationConfig")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class OfficeManagerConfiguration {
    OfficeManager officeManager;

    public OfficeManagerConfiguration(AppSettings settings) throws IOException {
        File dir = settings.getOfficeDirectory();

        this.officeManager = LocalOfficeManager.builder()
                .portNumbers(settings.getOpenOfficePorts())
                .workingDir(dir)
                .processTimeout(settings.getTimeout())
                .taskExecutionTimeout(settings.getTimeout())
                .maxTasksPerProcess(settings.getMaxTaskPerProcess())
                .existingProcessAction(ExistingProcessAction.CONNECT_OR_KILL)
                .officeHome(settings.getOpenOfficePath())
                .install()
                .build();
        try {
            officeManager.start();
        } catch (OfficeException e) {
            throw new ThumbnailerRuntimeException(e);
        }
    }

    @Bean("officeManager")
    OfficeManager getOfficeManager() {
        return officeManager;
    }

}
