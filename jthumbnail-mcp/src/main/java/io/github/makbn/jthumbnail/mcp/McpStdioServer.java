package io.github.makbn.jthumbnail.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Minimal MCP server over STDIO: newline-delimited JSON-RPC 2.0. Handles initialize,
 * tools/list_tools, and tools/call_tool so that LLMs can generate thumbnails via JThumbnail.
 */
public final class McpStdioServer {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final JThumbnailMcpClient client;

    public McpStdioServer(JThumbnailMcpClient client) {
        this.client = client;
    }

    /** Run the server loop: read from stdin, dispatch, write to stdout. Logs to stderr. */
    public void run() throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
                PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8), true)) {
            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String response = handleRequest(line);
                if (response != null) {
                    out.println(response);
                }
            }
        }
    }

    String handleRequest(String jsonLine) {
        try {
            JsonNode req = MAPPER.readTree(jsonLine);
            if (req == null || !req.isObject()) return errorResponse(null, -32700, "Parse error");
            String method = req.has("method") && req.get("method").isTextual()
                    ? req.get("method").asText()
                    : null;
            Object id = req.has("id") ? req.get("id") : null;
            JsonNode params = req.has("params") ? req.get("params") : null;

            if ("initialize".equals(method)) {
                return jsonRpcResult(id, buildInitializeResult());
            }
            if ("tools/list".equals(method)) {
                return jsonRpcResult(id, buildListToolsResult());
            }
            if ("tools/call".equals(method)) {
                return jsonRpcResult(id, handleCallTool(params));
            }
            if ("notifications/initialized".equals(method)) {
                return null;
            }
            return errorResponse(id, -32601, "Method not found: " + method);
        } catch (Exception e) {
            return errorResponse(null, -32603, "Internal error: " + e.getMessage());
        }
    }

    private static ObjectNode buildInitializeResult() {
        ObjectNode result = MAPPER.createObjectNode();
        result.put("protocolVersion", "2024-11-05");
        result.put(
                "serverInfo",
                MAPPER.createObjectNode().put("name", "jthumbnail-mcp").put("version", "1.0.0"));
        result.set("capabilities", MAPPER.createObjectNode().set("tools", MAPPER.createObjectNode()));
        return result;
    }

    private static ObjectNode buildListToolsResult() {
        ArrayNode tools = MAPPER.createArrayNode();

        ObjectNode genSchema = MAPPER.createObjectNode();
        genSchema.put("type", "object");
        genSchema.set("required", MAPPER.createArrayNode().add("file_path_or_url"));
        genSchema.set(
                "properties",
                MAPPER.createObjectNode()
                        .set(
                                "file_path_or_url",
                                MAPPER.createObjectNode()
                                        .put("type", "string")
                                        .put(
                                                "description",
                                                "Absolute path to a file or a URL (http/https/file) to thumbnail.")));
        tools.add(MAPPER.createObjectNode()
                .put("name", "generate_thumbnail")
                .put(
                        "description",
                        "Submit a file (local path or URL) to JThumbnail to generate a thumbnail. Returns the job ID.")
                .set("inputSchema", genSchema));

        ObjectNode statusSchema = MAPPER.createObjectNode();
        statusSchema.put("type", "object");
        statusSchema.set("required", MAPPER.createArrayNode().add("job_id"));
        statusSchema.set(
                "properties",
                MAPPER.createObjectNode()
                        .set(
                                "job_id",
                                MAPPER.createObjectNode()
                                        .put("type", "string")
                                        .put("description", "Job ID from generate_thumbnail.")));
        tools.add(MAPPER.createObjectNode()
                .put("name", "get_job_status")
                .put("description", "Get the status of a thumbnail job by ID (returned by generate_thumbnail).")
                .set("inputSchema", statusSchema));

        return MAPPER.createObjectNode().set("tools", tools);
    }

    private ObjectNode handleCallTool(JsonNode params) {
        ObjectNode content = MAPPER.createObjectNode();
        ArrayNode contentArray = MAPPER.createArrayNode();
        String name = params != null && params.has("name") && params.get("name").isTextual()
                ? params.get("name").asText()
                : null;
        JsonNode args = params != null && params.has("arguments") ? params.get("arguments") : null;

        if ("generate_thumbnail".equals(name)) {
            String filePathOrUrl = args != null
                            && args.has("file_path_or_url")
                            && args.get("file_path_or_url").isTextual()
                    ? args.get("file_path_or_url").asText()
                    : null;
            Optional<String> jobId = client.submitJob(filePathOrUrl);
            String text = jobId.map(id ->
                            "Job submitted. job_id=" + id + ". Use get_job_status with this job_id to check status.")
                    .orElse(
                            "Failed to submit job (check JThumbnail is running, webhook enabled, and file_path_or_url is valid).");
            contentArray.add(MAPPER.createObjectNode().put("type", "text").put("text", text));
        } else if ("get_job_status".equals(name)) {
            String jobId =
                    args != null && args.has("job_id") && args.get("job_id").isTextual()
                            ? args.get("job_id").asText()
                            : null;
            Optional<JThumbnailMcpClient.JobStatusDto> status = client.getJobStatus(jobId);
            String text = status.map(s -> "status=" + s.status
                            + (s.thumbnailPath != null ? ", thumbnailPath=" + s.thumbnailPath : "")
                            + (s.errorMessage != null ? ", error=" + s.errorMessage : ""))
                    .orElse("Job not found or request failed.");
            contentArray.add(MAPPER.createObjectNode().put("type", "text").put("text", text));
        } else {
            contentArray.add(MAPPER.createObjectNode().put("type", "text").put("text", "Unknown tool: " + name));
        }
        content.set("content", contentArray);
        return content;
    }

    private static String jsonRpcResult(Object id, JsonNode result) throws Exception {
        ObjectNode response = MAPPER.createObjectNode();
        response.put("jsonrpc", "2.0");
        if (id != null) response.set("id", MAPPER.valueToTree(id));
        response.set("result", result);
        return MAPPER.writeValueAsString(response);
    }

    private static String errorResponse(Object id, int code, String message) {
        try {
            ObjectNode response = MAPPER.createObjectNode();
            response.put("jsonrpc", "2.0");
            if (id != null) response.set("id", MAPPER.valueToTree(id));
            response.set("error", MAPPER.createObjectNode().put("code", code).put("message", message));
            return MAPPER.writeValueAsString(response);
        } catch (Exception e) {
            return "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32603,\"message\":\"Internal error\"}}";
        }
    }
}
