package io.github.makbn.jthumbnail.storage;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

/**
 * Configures S3 client for AWS S3 or MinIO (endpoint override).
 */
@Configuration
@ConditionalOnProperty(name = "jthumbnailer.storage.enabled", havingValue = "true")
@EnableConfigurationProperties(StorageProperties.class)
public class StorageClientConfig {

    @Bean
    public S3Client s3Client(StorageProperties props) {
        var builder = S3Client.builder()
                .region(Region.of(props.region()))
                .credentialsProvider(DefaultCredentialsProvider.create());
        if (props.endpointOverride() != null && !props.endpointOverride().isBlank()) {
            builder.endpointOverride(URI.create(props.endpointOverride().trim()));
        }
        return builder.build();
    }

    @Bean
    public SqsClient sqsClient(StorageProperties props) {
        var builder = SqsClient.builder()
                .region(Region.of(props.region()))
                .credentialsProvider(DefaultCredentialsProvider.create());
        if (props.endpointOverride() != null && !props.endpointOverride().isBlank()) {
            builder.endpointOverride(URI.create(props.endpointOverride().trim()));
        }
        return builder.build();
    }
}
