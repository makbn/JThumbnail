package io.github.makbn.jthumbnail.webhook;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WebhookSignatureValidatorTest {

    private final WebhookSignatureValidator validator = new WebhookSignatureValidator();

    @Test
    void validate_acceptsValidHmacSha256() throws Exception {
        String payload = "{\"fileUrl\":\"https://example.com/file.pdf\"}";
        String secret = "my-secret";
        String signature = "sha256=" + validator.computeHmacSha256(payload, secret);
        assertTrue(validator.validate(payload, signature, secret));
    }

    @Test
    void validate_rejectsWrongSecret() throws Exception {
        String payload = "{\"fileUrl\":\"https://example.com/file.pdf\"}";
        String sig = "sha256=" + validator.computeHmacSha256(payload, "secret1");
        assertFalse(validator.validate(payload, sig, "secret2"));
    }

    @Test
    void validate_rejectsBlankSignature() {
        assertFalse(validator.validate("body", "", "secret"));
        assertFalse(validator.validate("body", null, "secret"));
    }

    @Test
    void validate_acceptsSignatureWithoutSha256Prefix() throws Exception {
        String payload = "hello";
        String secret = "key";
        String signature = validator.computeHmacSha256(payload, secret);
        assertTrue(validator.validate(payload, signature, secret));
    }
}
