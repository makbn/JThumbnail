package io.github.makbn.jthumbnail.grpc.server;

import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

/**
 * Starts the gRPC server when enabled. Supports plain and TLS.
 */
@Component
@ConditionalOnProperty(name = "jthumbnailer.grpc.enabled", havingValue = "true")
@EnableConfigurationProperties(GrpcProperties.class)
@Slf4j
public class GrpcServerRunner implements SmartLifecycle {

    private final GrpcProperties props;
    private final ThumbnailGrpcServiceImpl thumbnailService;
    private Server server;
    private boolean running;

    public GrpcServerRunner(GrpcProperties props, ThumbnailGrpcServiceImpl thumbnailService) {
        this.props = props;
        this.thumbnailService = thumbnailService;
    }

    @Override
    public void start() {
        if (server != null) return;
        try {
            var builder = NettyServerBuilder.forPort(props.port())
                    .addService(thumbnailService)
                    .maxInboundMessageSize(32 * 1024 * 1024); // 32MB for large uploads
            if (Boolean.TRUE.equals(props.useTls())
                    && props.certChainFile() != null
                    && props.privateKeyFile() != null) {
                var certFile = new File(props.certChainFile());
                var keyFile = new File(props.privateKeyFile());
                if (certFile.canRead() && keyFile.canRead()) {
                    builder.useTransportSecurity(certFile, keyFile);
                    log.info(
                            "gRPC server TLS enabled (cert: {}, key: {})",
                            props.certChainFile(),
                            props.privateKeyFile());
                } else {
                    log.warn("gRPC useTls is true but cert/key files not readable; starting plain");
                }
            }
            server = builder.build().start();
            running = true;
            log.info("gRPC server started on port {}", props.port());
        } catch (SSLException e) {
            throw new IllegalStateException("gRPC TLS configuration failed: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new IllegalStateException("gRPC server failed to start: " + e.getMessage(), e);
        }
    }

    @Override
    public void stop() {
        if (server == null) return;
        running = false;
        server.shutdown();
        try {
            if (!server.awaitTermination(10, TimeUnit.SECONDS)) {
                server.shutdownNow();
                server.awaitTermination(5, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            server.shutdownNow();
            Thread.currentThread().interrupt();
        }
        server = null;
        log.info("gRPC server stopped");
    }

    @Override
    public boolean isRunning() {
        return running && server != null && !server.isShutdown();
    }
}
