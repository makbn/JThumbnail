package io.github.makbn.jthumbnail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Minimal Spring Boot configuration for core module tests that use @SpringBootTest.
 * Scans only the jthumbnail core packages.
 */
@SpringBootApplication(scanBasePackages = "io.github.makbn.jthumbnail")
public class CoreTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreTestApplication.class, args);
    }
}
