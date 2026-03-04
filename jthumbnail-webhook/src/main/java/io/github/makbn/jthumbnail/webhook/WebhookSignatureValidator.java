package io.github.makbn.jthumbnail.webhook;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Validates HMAC signature of webhook payloads (e.g. HMAC-SHA256).
 */
@Component
@Slf4j
public class WebhookSignatureValidator {

    private static final String HMAC_SHA256 = "HmacSHA256";

    /**
     * Verify that the given signature matches HMAC-SHA256 of the body with the secret.
     *
     * @param rawBody    raw request body (UTF-8)
     * @param signature  value from header (e.g. "sha256=base64..." or just "base64...")
     * @param secret     shared secret (UTF-8)
     * @return true if signature is valid
     */
    public boolean validate(String rawBody, String signature, String secret) {
        if (signature == null || signature.isBlank() || secret == null || secret.isBlank()) {
            return false;
        }
        try {
            String expected = computeHmacSha256(rawBody, secret);
            String received = signature.trim();
            if (received.toLowerCase().startsWith("sha256=")) {
                received = received.substring(7).trim();
            }
            return java.security.MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8), received.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.debug("HMAC validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String computeHmacSha256(String payload, String secret)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(HMAC_SHA256);
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
        byte[] hmac = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hmac);
    }
}
