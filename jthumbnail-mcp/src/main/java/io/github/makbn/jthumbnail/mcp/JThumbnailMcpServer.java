package io.github.makbn.jthumbnail.mcp;

/**
 * Entry point for the JThumbnail MCP server. Runs a stdio JSON-RPC server so that LLM clients
 * (e.g. Claude Desktop, Cursor) can call tools to generate thumbnails and check job status.
 * <p>
 * Requires a running JThumbnail instance with webhook enabled. Configure the base URL via
 * environment variable {@code JTHUMBNAIL_BASE_URL} (default: {@code http://localhost:8081}).
 * <p>
 * Example (Claude Desktop): add to your config and run this process as the MCP server command.
 */
public final class JThumbnailMcpServer {

    private static final String DEFAULT_BASE_URL = "http://localhost:8081";

    public static void main(String[] args) {
        String baseUrl = System.getenv("JTHUMBNAIL_BASE_URL");
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = DEFAULT_BASE_URL;
        }
        JThumbnailMcpClient client = new JThumbnailMcpClient(baseUrl);
        McpStdioServer server = new McpStdioServer(client);
        try {
            server.run();
        } catch (Exception e) {
            System.err.println("MCP server error: " + e.getMessage());
            System.exit(1);
        }
    }
}
