package io.github.makbn.jthumbnail.core.properties;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.net.URI;

/**
 * OpenAPI properties containing the fields used on Swagger page
 *
 * @param name       Application name
 * @param desc       Application description
 * @param license    License for the API
 * @param licenseURL URL of the application
 **/
@ConfigurationProperties(prefix = "jthumbnailer.openapi", ignoreUnknownFields = false)
@Validated
public record OpenAPIProperties(
        @NotNull @NotEmpty @DefaultValue("Java Thumbnail Generator") String name,
        @NotNull @DefaultValue("A thumbnail generation Java library") String desc,
        @NotNull @NotEmpty @DefaultValue("GPL-2.0") String license,
        @NotNull @DefaultValue("https://github.com/makbn/JThumbnail/blob/master/LICENSE") URI licenseURL) {}
