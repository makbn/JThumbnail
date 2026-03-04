package io.github.makbn.jthumbnail.webservice;

import io.github.makbn.jthumbnail.connector.api.JobProducer;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "io.github.makbn")
@EnableJpaRepositories(basePackages = "io.github.makbn.jthumbnail.core.job")
@EntityScan(basePackages = "io.github.makbn.jthumbnail.core.job")
public class WebserviceTestApplication {

    @Bean
    JobProducer testJobProducer() {
        return new JobProducer() {
            @Override
            public void sendJob(String jobId) {
                // no-op for tests
            }

            @Override
            public void sendToDeadLetter(String jobId) {
                // no-op for tests
            }
        };
    }
}
