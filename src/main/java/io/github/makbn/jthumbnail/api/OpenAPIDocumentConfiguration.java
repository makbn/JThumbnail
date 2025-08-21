package io.github.makbn.jthumbnail.api;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIDocumentConfiguration {
    @Bean
    public OpenAPI customOpenAPI(
            @Value("${jthumbnailer.name}") String title, @Value("${jthumbnailer.desc}") String desc) {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .summary(desc)
                        .title(title)
                        .license(new License()
                                .name("GPL-2.0")
                                .url("https://github.com/makbn/JThumbnail/blob/master/LICENSE")));
    }
}
