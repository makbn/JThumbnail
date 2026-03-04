package io.github.makbn.jthumbnail.webservice.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import io.github.makbn.jthumbnail.core.config.RateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-client rate limit for POST {@code /} (upload) and POST {@code /jobs/{id}/retry}.
 * Uses Bucket4j; responds with 429 when limit exceeded.
 */
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties props;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitFilter(RateLimitProperties props) {
        this.props = props;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        if (!props.enabled()) {
            filterChain.doFilter(request, response);
            return;
        }
        String key = clientKey(request);
        Bucket bucket = buckets.computeIfAbsent(key, k -> createBucket());
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":true,\"message\":\"Rate limit exceeded\"}");
        }
    }

    /** Only apply to POST upload and POST retry. */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        if (path == null) return true;
        if (path.equals("/")) return false;
        if (path.matches("/jobs/[^/]+/retry")) return false;
        return true;
    }

    private String clientKey(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.classic(
                props.requestsPerMinute(), Refill.greedy(props.requestsPerMinute(), Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
}
