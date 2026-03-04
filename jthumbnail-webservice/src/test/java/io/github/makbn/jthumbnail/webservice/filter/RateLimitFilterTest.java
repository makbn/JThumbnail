package io.github.makbn.jthumbnail.webservice.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.makbn.jthumbnail.core.config.RateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.PrintWriter;
import java.io.StringWriter;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RateLimitFilterTest {

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    FilterChain chain;

    private RateLimitFilter filter;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        responseWriter = new StringWriter();
    }

    @Test
    void whenDisabledPassesThrough() throws Exception {
        filter = new RateLimitFilter(new RateLimitProperties(false, 60));
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/");

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }

    @Test
    void getRequestNotFiltered() throws Exception {
        filter = new RateLimitFilter(new RateLimitProperties(true, 1));
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/jobs");

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void postUploadWithinLimitPassesThrough() throws Exception {
        filter = new RateLimitFilter(new RateLimitProperties(true, 10));
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void postRetryWithinLimitPassesThrough() throws Exception {
        filter = new RateLimitFilter(new RateLimitProperties(true, 10));
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/jobs/abc-123/retry");
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void whenLimitExceededReturns429() throws Exception {
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        filter = new RateLimitFilter(new RateLimitProperties(true, 1));
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        filter.doFilterInternal(request, response, chain);
        filter.doFilterInternal(request, response, chain);

        verify(response).setStatus(429);
        assertEquals(
                "{\"error\":true,\"message\":\"Rate limit exceeded\"}",
                responseWriter.toString().trim());
    }
}
