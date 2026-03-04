package io.github.makbn.jthumbnail.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

/**
 * HTTP client for the JThumbnail webhook and jobs API. Used by the MCP server to submit
 * thumbnail jobs and query status without depending on Spring or the full application context.
 */
public class JThumbnailMcpClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String baseUrl;
    private final HttpClient httpClient;

    public JThumbnailMcpClient(String baseUrl) {
        this(
                baseUrl,
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build());
    }

    public JThumbnailMcpClient(String baseUrl, HttpClient httpClient) {
        String normalized = baseUrl == null ? "http://localhost:8081" : baseUrl.trim();
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        this.baseUrl = normalized;
        this.httpClient = httpClient;
    }

    /**
     * Submit a thumbnail job via the webhook endpoint. The webhook must be enabled and
     * accept JSON with {@code fileUrl} (or {@code url} / {@code source_url}).
     *
     * @param filePathOrUrl local path (e.g. /path/to/file.pdf) or URL (http(s) or file://)
     * @return the created job id, or empty on failure
     */
    public Optional<String> submitJob(String filePathOrUrl) {
        if (filePathOrUrl == null || filePathOrUrl.isBlank()) {
            return Optional.empty();
        }
        String payload = "{\"fileUrl\":\"" + escapeJsonString(filePathOrUrl.trim()) + "\"}";
        URI uri = URI.create(baseUrl + "/webhook");
        HttpRequest request = HttpRequest.newBuilder(uri)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();
        try {
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200 && response.statusCode() != 202) {
                return Optional.empty();
            }
            JsonNode body = MAPPER.readTree(response.body());
            JsonNode jobId = body != null ? body.get("jobId") : null;
            return jobId != null && jobId.isTextual() ? Optional.of(jobId.asText()) : Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Get job status from the jobs API.
     *
     * @param jobId job id returned by {@link #submitJob(String)}
     * @return status summary (status, thumbnailPath, errorMessage, etc.) or empty if not found/failed
     */
    public Optional<JobStatusDto> getJobStatus(String jobId) {
        if (jobId == null || jobId.isBlank()) {
            return Optional.empty();
        }
        URI uri = URI.create(baseUrl + "/jobs/" + jobId.trim());
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        try {
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                return Optional.empty();
            }
            JsonNode root = MAPPER.readTree(response.body());
            if (root == null || root.get("error").asBoolean(false)) {
                return Optional.empty();
            }
            JsonNode result = root.get("result");
            if (result == null || !result.isObject()) {
                return Optional.empty();
            }
            JobStatusDto dto = new JobStatusDto();
            dto.jobId = text(result, "jobId");
            dto.filePath = text(result, "filePath");
            dto.status = text(result, "status");
            dto.thumbnailPath = text(result, "thumbnailPath");
            dto.errorMessage = text(result, "errorMessage");
            dto.retryCount = result.has("retryCount") ? result.get("retryCount").asInt(0) : 0;
            return Optional.of(dto);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static String text(JsonNode node, String key) {
        JsonNode n = node.get(key);
        return n != null && n.isTextual() ? n.asText() : null;
    }

    private static String escapeJsonString(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /** DTO for job status returned by GET /jobs/{id}. */
    public static class JobStatusDto {
        public String jobId;
        public String filePath;
        public String status;
        public String thumbnailPath;
        public String errorMessage;
        public int retryCount;
    }
}
