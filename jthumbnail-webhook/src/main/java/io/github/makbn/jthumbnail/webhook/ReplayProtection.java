package io.github.makbn.jthumbnail.webhook;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory replay protection: remembers seen idempotency keys within a time window.
 */
@Component
@Slf4j
public class ReplayProtection {

    private final Map<String, Long> seen = new ConcurrentHashMap<>();
    private final AtomicLong evictionRuns = new AtomicLong(0);

    private static final long EVICTION_INTERVAL_MS = 60_000;

    private long lastEviction = System.currentTimeMillis();

    /**
     * Check if the key was already seen within the given window (seconds). If not, record it.
     *
     * @param idempotencyKey key (e.g. from X-Idempotency-Key header)
     * @param windowSeconds  replay window in seconds
     * @return true if this is a replay (already seen), false if new and recorded
     */
    public boolean isReplay(String idempotencyKey, int windowSeconds) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return false;
        }
        evictIfNeeded(windowSeconds);
        long now = System.currentTimeMillis();
        long cutoff = now - windowSeconds * 1000L;
        Long existing = seen.putIfAbsent(idempotencyKey, now);
        if (existing != null && existing >= cutoff) {
            log.debug("Replay detected for key: {}", idempotencyKey);
            return true;
        }
        if (existing != null) {
            seen.put(idempotencyKey, now);
        }
        return false;
    }

    private void evictIfNeeded(int windowSeconds) {
        long now = System.currentTimeMillis();
        if (now - lastEviction < EVICTION_INTERVAL_MS) {
            return;
        }
        synchronized (this) {
            if (now - lastEviction < EVICTION_INTERVAL_MS) return;
            lastEviction = now;
        }
        long cutoff = now - windowSeconds * 1000L;
        seen.entrySet().removeIf(e -> e.getValue() < cutoff);
        evictionRuns.incrementAndGet();
    }
}
