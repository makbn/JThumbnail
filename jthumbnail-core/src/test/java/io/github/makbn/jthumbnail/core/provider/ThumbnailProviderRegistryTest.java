package io.github.makbn.jthumbnail.core.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.makbn.jthumbnail.core.exception.ThumbnailException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

class ThumbnailProviderRegistryTest {

    @TempDir
    Path tempDir;

    private ThumbnailProviderRegistry registry;
    private ThumbnailProvider successProvider;
    private ThumbnailProvider failProvider;

    @BeforeEach
    void setUp() {
        successProvider = new StubProvider("First", "image/png", true);
        failProvider = new StubProvider("Second", "image/png", false);
        ProviderRegistryProperties props = new ProviderRegistryProperties(List.of("Second", "First"));
        registry = new ThumbnailProviderRegistry(List.of(failProvider, successProvider), null, props);
    }

    @Test
    void getProvidersForReturnsSupportingProvidersInPriorityOrder() {
        List<ThumbnailProvider> providers = registry.getProvidersFor(FileType.of("image/png"));
        assertEquals(2, providers.size());
        assertEquals("Second", providers.get(0).getName());
        assertEquals("First", providers.get(1).getName());
    }

    @Test
    void getProvidersForReturnsEmptyForUnsupportedType() {
        List<ThumbnailProvider> providers = registry.getProvidersFor(FileType.of("application/unknown"));
        assertTrue(providers.isEmpty());
    }

    @Test
    void generateThumbnailUsesFirstSuccessfulProvider() throws Exception {
        File in = tempDir.resolve("in.png").toFile();
        File out = tempDir.resolve("out.png").toFile();
        in.createNewFile();
        registry.generateThumbnail(in, out, "image/png");
        assertTrue(out.exists());
    }

    @Test
    void generateThumbnailThrowsWhenNoProviderSupportsType() throws Exception {
        File in = tempDir.resolve("in.xyz").toFile();
        File out = tempDir.resolve("out.png").toFile();
        in.createNewFile();
        ThumbnailException ex = assertThrows(
                ThumbnailException.class, () -> registry.generateThumbnail(in, out, "application/unknown"));
        assertTrue(ex.getMessage().contains("No provider supports"));
    }

    @Test
    void registerAndUnregisterRuntimeProvider() {
        ThumbnailProvider runtime = new StubProvider("Runtime", "video/mp4", true);
        registry.register(runtime);
        List<ThumbnailProvider> providers = registry.getProvidersFor(FileType.of("video/mp4"));
        assertEquals(1, providers.size());
        assertEquals("Runtime", providers.get(0).getName());
        registry.unregister(runtime);
        assertTrue(registry.getProvidersFor(FileType.of("video/mp4")).isEmpty());
    }

    @Test
    void registerIgnoresNullAndDuplicate() {
        registry.register(null);
        registry.register(successProvider);
        registry.register(successProvider);
        List<ThumbnailProvider> providers = registry.getProvidersFor(FileType.of("image/png"));
        assertEquals(3, providers.size());
    }

    @Test
    void closeClearsRuntimeProviders() throws IOException {
        ThumbnailProvider runtime = new StubProvider("Runtime", "video/mp4", true);
        registry.register(runtime);
        registry.close();
        assertTrue(registry.getProvidersFor(FileType.of("video/mp4")).isEmpty());
    }

    private static class StubProvider implements ThumbnailProvider {
        final String name;
        final String supportedMime;
        final boolean succeed;

        StubProvider(String name, String supportedMime, boolean succeed) {
            this.name = name;
            this.supportedMime = supportedMime;
            this.succeed = succeed;
        }

        @Override
        public boolean supports(FileType fileType) {
            return fileType != null && supportedMime.equals(fileType.mimeType());
        }

        @Override
        public void generateThumbnail(File input, File output, String mimeType) throws IOException, ThumbnailException {
            if (!succeed) {
                throw new ThumbnailException("stub fail");
            }
            output.getParentFile().mkdirs();
            output.createNewFile();
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
