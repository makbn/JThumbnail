package io.github.makbn.jthumbnail.core.provider;

import io.github.makbn.jthumbnail.core.exception.ThumbnailException;
import io.github.makbn.jthumbnail.core.exception.ThumbnailRuntimeException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registry of thumbnail providers. Resolves providers by file type, tries in priority order,
 * and supports runtime registration via {@link #register(ThumbnailProvider)}.
 */
@Component
@Slf4j
@EnableConfigurationProperties(ProviderRegistryProperties.class)
public class ThumbnailProviderRegistry {

    private final List<ThumbnailProvider> orderedProviders;
    private final List<ThumbnailProvider> runtimeProviders = new CopyOnWriteArrayList<>();

    public ThumbnailProviderRegistry(
            List<ThumbnailProvider> beanProviders,
            List<io.github.makbn.jthumbnail.core.thumbnailers.Thumbnailer> thumbnailers,
            ProviderRegistryProperties props) {
        List<ThumbnailProvider> combined = new ArrayList<>();
        if (beanProviders != null) {
            combined.addAll(beanProviders);
        }
        if (thumbnailers != null) {
            thumbnailers.stream()
                    .filter(t -> !(t instanceof io.github.makbn.jthumbnail.core.ThumbnailerManager))
                    .map(ThumbnailerAdapter::new)
                    .forEach(combined::add);
        }
        this.orderedProviders = sortByPriority(combined, props);
        log.info("ThumbnailProviderRegistry loaded {} providers (priority order)", orderedProviders.size());
    }

    private static List<ThumbnailProvider> sortByPriority(
            List<ThumbnailProvider> providers, ProviderRegistryProperties props) {
        List<String> priority = props.priority() != null ? props.priority() : List.of();
        return providers.stream()
                .sorted(Comparator.comparingInt(p -> {
                    int idx = priority.indexOf(p.getName());
                    return idx >= 0 ? idx : Integer.MAX_VALUE;
                }))
                .toList();
    }

    /** Add a provider at runtime (e.g. SPI or dynamic loading). */
    public void register(ThumbnailProvider provider) {
        if (provider != null && !runtimeProviders.contains(provider)) {
            runtimeProviders.add(provider);
            log.debug("Registered provider at runtime: {}", provider.getName());
        }
    }

    /** Remove a runtime-registered provider. */
    public void unregister(ThumbnailProvider provider) {
        runtimeProviders.remove(provider);
    }

    /** Providers that support the file type, in priority order (beans first, then runtime). */
    public List<ThumbnailProvider> getProvidersFor(FileType fileType) {
        List<ThumbnailProvider> out = new ArrayList<>();
        for (ThumbnailProvider p : orderedProviders) {
            if (p.supports(fileType)) out.add(p);
        }
        for (ThumbnailProvider p : runtimeProviders) {
            if (p.supports(fileType)) out.add(p);
        }
        return out;
    }

    /** Run first supporting provider until one succeeds; throws if none do. */
    public void generateThumbnail(File input, File output, String mimeType)
            throws ThumbnailException, ThumbnailRuntimeException {
        FileType fileType = FileType.of(mimeType);
        List<ThumbnailProvider> providers = getProvidersFor(fileType);
        if (providers.isEmpty()) {
            throw new ThumbnailException("No provider supports file type: " + mimeType);
        }
        ThumbnailException lastException = null;
        for (ThumbnailProvider provider : providers) {
            try {
                log.debug("Trying provider {} for {}", provider.getName(), input.getName());
                provider.generateThumbnail(input, output, mimeType);
                return;
            } catch (ThumbnailRuntimeException e) {
                log.warn("Provider {} failed: {}", provider.getName(), e.getMessage());
                lastException = new ThumbnailException(e);
            } catch (ThumbnailException e) {
                log.debug("Provider {} declined: {}", provider.getName(), e.getMessage());
                lastException = e;
            } catch (IOException e) {
                log.debug("Provider {} I/O error: {}", provider.getName(), e.getMessage());
                lastException = new ThumbnailException(e);
            } catch (Exception e) {
                log.error("Provider {} error", provider.getName(), e);
                lastException = new ThumbnailException(e);
            }
        }
        throw lastException != null ? lastException : new ThumbnailException("No provider could generate thumbnail");
    }

    /** Close closeable providers and clear runtime-registered ones. */
    public void close() {
        for (ThumbnailProvider p : orderedProviders) {
            if (p instanceof Closeable c) {
                try {
                    c.close();
                } catch (IOException e) {
                    log.warn("Error closing provider {}: {}", p.getName(), e.getMessage());
                }
            }
        }
        runtimeProviders.clear();
    }
}
