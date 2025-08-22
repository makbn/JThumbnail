package io.github.makbn.jthumbnail.core.config;

import io.github.makbn.jthumbnail.core.exception.ThumbnailerRuntimeException;
import io.github.makbn.jthumbnail.core.properties.OfficeProperties;
import lombok.RequiredArgsConstructor;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.office.ExistingProcessAction;
import org.jodconverter.local.office.LocalOfficeManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class OfficeManagerConfiguration {

    private final OfficeProperties officeProperties;

    private OfficeManager officeManager = null;

    @Bean("officeManager")
    OfficeManager getOfficeManager() {
        if (officeManager == null) {
            this.officeManager = LocalOfficeManager.builder()
                    .portNumbers(officeProperties.ports().stream()
                            .mapToInt(Integer::valueOf)
                            .toArray())
                    .workingDir(officeProperties.workingDir())
                    .processTimeout(officeProperties.timeout())
                    .taskExecutionTimeout(officeProperties.timeout())
                    .maxTasksPerProcess(officeProperties.maxTasksPerProcess())
                    .existingProcessAction(ExistingProcessAction.CONNECT_OR_KILL)
                    .officeHome(officeProperties.officeHome())
                    .install()
                    .build();
            try {
                officeManager.start();
            } catch (OfficeException e) {
                throw new ThumbnailerRuntimeException(e);
            }
        }
        return officeManager;
    }
}
