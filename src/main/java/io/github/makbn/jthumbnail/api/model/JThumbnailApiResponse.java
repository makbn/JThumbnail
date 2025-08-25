package io.github.makbn.jthumbnail.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.net.HttpURLConnection;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JThumbnailApiResponse<T> {
    @Builder.Default
    @Schema(description = "The HTTP status code of the response", example = "200")
    int code = HttpURLConnection.HTTP_OK;

    @Builder.Default
    @Schema(description = "A message describing the result of the operation", example = "OK")
    String message = "OK";

    @Schema(description = "The result of the operation", example = "your_result_data_here")
    T result;

    @Schema(description = "Flag indicating whether an error occurred", example = "false")
    boolean error;
}
