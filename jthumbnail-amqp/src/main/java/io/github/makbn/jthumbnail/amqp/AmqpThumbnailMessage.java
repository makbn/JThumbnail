package io.github.makbn.jthumbnail.amqp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * AMQP message schema for thumbnail requests. Supports file URL, optional storage type and desired size.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AmqpThumbnailMessage {

    /** URL of the file to thumbnail: file:// path or http(s):// URL (downloaded to temp). */
    @NotBlank
    @JsonProperty("fileUrl")
    private String fileUrl;

    /** Optional storage type hint (e.g. LOCAL, S3). Reserved for future use. */
    @JsonProperty("storageType")
    private String storageType;

    /** Optional desired max size (e.g. max width/height in pixels). Reserved for future use. */
    @PositiveOrZero
    @JsonProperty("desiredSize")
    private Integer desiredSize;
}
