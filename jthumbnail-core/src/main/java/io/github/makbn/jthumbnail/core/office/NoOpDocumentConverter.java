package io.github.makbn.jthumbnail.core.office;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFormatRegistry;
import org.jodconverter.core.job.ConversionJobWithOptionalSourceFormatUnspecified;
import org.jodconverter.core.job.ConversionJobWithOptionalTargetFormatUnspecified;
import org.jodconverter.core.office.OfficeException;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * No-op {@link DocumentConverter} for tests or when LibreOffice is not available.
 * {@link #convert(File)} returns a job that throws
 * {@link UnsupportedOperationException} on {@code execute()}.
 */
public final class NoOpDocumentConverter implements DocumentConverter {

    private static final DocumentFormatRegistry REGISTRY = DefaultDocumentFormatRegistry.getInstance();

    @Override
    public DocumentFormatRegistry getFormatRegistry() {
        return REGISTRY;
    }

    @Override
    public ConversionJobWithOptionalSourceFormatUnspecified convert(File source) {
        return jobChain(source);
    }

    @Override
    public ConversionJobWithOptionalSourceFormatUnspecified convert(InputStream source) {
        return jobChain(null);
    }

    @Override
    public ConversionJobWithOptionalSourceFormatUnspecified convert(InputStream source, boolean closeStream) {
        return jobChain(null);
    }

    @SuppressWarnings("unchecked")
    private static ConversionJobWithOptionalSourceFormatUnspecified jobChain(Object source) {
        InvocationHandler targetJobHandler = (proxy, method, args) -> {
            if ("execute".equals(method.getName())) {
                throw new OfficeException("NoOpDocumentConverter: LibreOffice is not configured");
            }
            if ("toString".equals(method.getName())) {
                return "NoOpConversionJob";
            }
            return null;
        };
        Object targetJob = Proxy.newProxyInstance(
                ConversionJobWithOptionalTargetFormatUnspecified.class.getClassLoader(),
                new Class<?>[] {ConversionJobWithOptionalTargetFormatUnspecified.class},
                targetJobHandler);
        InvocationHandler sourceJobHandler = (proxy, method, args) -> {
            if ("to".equals(method.getName()) && args != null && args.length == 1) {
                return targetJob;
            }
            if ("as".equals(method.getName()) && args != null && args.length == 1) {
                return proxy;
            }
            if ("toString".equals(method.getName())) {
                return "NoOpConversionJob";
            }
            return null;
        };
        return (ConversionJobWithOptionalSourceFormatUnspecified) Proxy.newProxyInstance(
                ConversionJobWithOptionalSourceFormatUnspecified.class.getClassLoader(),
                new Class<?>[] {ConversionJobWithOptionalSourceFormatUnspecified.class},
                sourceJobHandler);
    }
}
