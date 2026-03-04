package io.github.makbn.jthumbnail.cdnedge;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Downloads CDN / arbitrary HTTP(s) URLs to a local temporary file, applying
 * basic safety constraints (allowed hosts, extensions, size, timeout).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CdnEdgeDownloadService {

    private final CdnEdgeProperties props;

    private RestTemplate buildClient() {
        SimpleClientHttpRequestFactory base = new SimpleClientHttpRequestFactory();
        int timeoutMs = Math.toIntExact(props.downloadTimeout().toMillis());
        base.setConnectTimeout(timeoutMs);
        base.setReadTimeout(timeoutMs);
        return new RestTemplate(new BufferingClientHttpRequestFactory(base));
    }

    public File downloadToTemp(String url) throws IOException {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URL: " + url, e);
        }

        if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) {
            throw new IOException("Unsupported scheme (expected http/https): " + uri.getScheme());
        }

        if (!hostAllowed(uri.getHost())) {
            throw new IOException("Host not allowed: " + uri.getHost());
        }

        if (!extensionAllowed(uri.getPath())) {
            throw new IOException("File extension not allowed: " + uri.getPath());
        }

        RestTemplate client = buildClient();
        var response = client.execute(
                uri,
                org.springframework.http.HttpMethod.GET,
                null,
                clientHttpResponse -> {
                    if (!Objects.equals(responseStatus(clientHttpResponse.getRawStatusCode()), HttpStatus.OK)) {
                        throw new RestClientException("Unexpected response status: " + clientHttpResponse.getRawStatusCode());
                    }
                    long contentLength = contentLength(clientHttpResponse.getHeaders());
                    if (contentLength > 0 && contentLength > props.maxBytes()) {
                        throw new RestClientException("Content-Length exceeds limit: " + contentLength);
                    }
                    Path tempFile =
                            Files.createTempFile("cdn-thumb-", "-" + fileName(uri.getPath()));
                    try (var in = clientHttpResponse.getBody();
                            var out = new FileOutputStream(tempFile.toFile())) {
                        byte[] data = in.readAllBytes();
                        long length = Math.min(data.length, props.maxBytes() + 1);
                        out.write(data, 0, (int) length);
                        if (length > props.maxBytes()) {
                            throw new RestClientException("Downloaded bytes exceed limit: " + length);
                        }
                    }
                    return tempFile.toFile();
                });

        if (response == null) {
            throw new IOException("Empty HTTP response for " + url);
        }
        return response;
    }

    private boolean hostAllowed(String host) {
        if (!StringUtils.hasText(host)) return false;
        List<String> allowed = props.allowedHosts();
        if (allowed == null || allowed.isEmpty()) return true;
        return allowed.stream().anyMatch(h -> h.equalsIgnoreCase(host));
    }

    private boolean extensionAllowed(String path) {
        List<String> allowed = props.allowedExtensions();
        if (allowed == null || allowed.isEmpty()) return true;
        String ext = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1).toLowerCase() : "";
        return allowed.stream().map(String::toLowerCase).anyMatch(ext::equals);
    }

    private static HttpStatus responseStatus(int rawStatus) {
        try {
            return HttpStatus.valueOf(rawStatus);
        } catch (Exception e) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    private static long contentLength(HttpHeaders headers) {
        return headers != null ? headers.getContentLength() : -1L;
    }

    private static String fileName(String path) {
        if (path == null || path.isBlank()) {
            return "download.bin";
        }
        String name = Path.of(path).getFileName().toString();
        return name.isBlank() ? "download.bin" : name;
    }
}

