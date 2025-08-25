package io.github.makbn.jthumbnail.api;

import io.github.makbn.jthumbnail.core.properties.OpenAPIProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class OpenAPIDocumentConfiguration {

    @Bean
    OpenAPI customOpenAPI(OpenAPIProperties apiProperties) {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .summary(apiProperties.desc())
                        .title(apiProperties.name())
                        .license(new License()
                                .name(apiProperties.license())
                                .url(apiProperties.licenseURL().toString())));
    }
}
