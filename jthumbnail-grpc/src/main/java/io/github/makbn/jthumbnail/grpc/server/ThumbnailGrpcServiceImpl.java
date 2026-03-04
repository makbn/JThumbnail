package io.github.makbn.jthumbnail.grpc.server;

import io.github.makbn.jthumbnail.connector.api.ThumbnailJobSubmitter;
import io.github.makbn.jthumbnail.core.config.ThumbnailServerConfiguration;
import io.github.makbn.jthumbnail.core.job.ThumbnailJob;
import io.github.makbn.jthumbnail.core.job.ThumbnailJobService;
import io.github.makbn.jthumbnail.core.metrics.ThumbnailMetrics;
import io.github.makbn.jthumbnail.grpc.GenerateThumbnailResponse;
import io.github.makbn.jthumbnail.grpc.GetJobStatusRequest;
import io.github.makbn.jthumbnail.grpc.GetJobStatusResponse;
import io.github.makbn.jthumbnail.grpc.ThumbnailServiceGrpc;
import io.github.makbn.jthumbnail.grpc.UploadChunk;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Optional;
import java.util.UUID;

/**
 * gRPC service implementation: streaming upload and job status.
 */
@Service
@ConditionalOnProperty(name = "jthumbnailer.grpc.enabled", havingValue = "true")
@Slf4j
public class ThumbnailGrpcServiceImpl extends ThumbnailServiceGrpc.ThumbnailServiceImplBase {

    private final ThumbnailJobService jobService;
    private final ThumbnailJobSubmitter jobSubmitter;
    private final ThumbnailMetrics metrics;
    private final ThumbnailServerConfiguration serverConfig;

    public ThumbnailGrpcServiceImpl(
            ThumbnailJobService jobService,
            ThumbnailJobSubmitter jobSubmitter,
            ThumbnailMetrics metrics,
            ThumbnailServerConfiguration serverConfig) {
        this.jobService = jobService;
        this.jobSubmitter = jobSubmitter;
        this.metrics = metrics;
        this.serverConfig = serverConfig;
    }

    @Override
    public StreamObserver<UploadChunk> generateThumbnail(StreamObserver<GenerateThumbnailResponse> responseObserver) {
        return new StreamObserver<>() {
            String filename = "upload-" + UUID.randomUUID();
            OutputStream fileOut = null;
            java.nio.file.Path tempPath = null;
            boolean metaReceived = false;

            @Override
            public void onNext(UploadChunk chunk) {
                try {
                    switch (chunk.getPayloadCase()) {
                        case META -> {
                            if (chunk.hasMeta()
                                    && !chunk.getMeta().getFilename().isBlank()) {
                                filename = chunk.getMeta().getFilename();
                            }
                            metaReceived = true;
                        }
                        case CHUNK -> {
                            if (fileOut == null) {
                                java.io.File uploadDir = serverConfig.getUploadDirectory();
                                tempPath = uploadDir.toPath().resolve(UUID.randomUUID() + "-" + filename);
                                fileOut = Files.newOutputStream(tempPath);
                            }
                            if (chunk.getChunk() != null && chunk.getChunk().size() > 0) {
                                chunk.getChunk().writeTo(fileOut);
                            }
                        }
                        default -> {}
                    }
                } catch (IOException e) {
                    onError(e);
                }
            }

            @Override
            public void onError(Throwable t) {
                log.warn("GenerateThumbnail stream error: {}", t.getMessage());
                closeQuietly();
                if (tempPath != null) {
                    try {
                        Files.deleteIfExists(tempPath);
                    } catch (IOException ignored) {
                    }
                }
                responseObserver.onError(io.grpc.Status.INTERNAL
                        .withCause(t)
                        .withDescription(t.getMessage())
                        .asException());
            }

            @Override
            public void onCompleted() {
                closeQuietly();
                if (tempPath == null || !Files.isRegularFile(tempPath)) {
                    responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                            .withDescription("No file data received")
                            .asException());
                    return;
                }
                try {
                    String jobId = jobSubmitter.submit(tempPath.toAbsolutePath().toString());
                    responseObserver.onNext(GenerateThumbnailResponse.newBuilder()
                            .setJobId(jobId)
                            .build());
                    responseObserver.onCompleted();
                } catch (Exception e) {
                    log.error("Failed to create job for gRPC upload: {}", e.getMessage());
                    try {
                        Files.deleteIfExists(tempPath);
                    } catch (IOException ignored) {
                    }
                    responseObserver.onError(io.grpc.Status.INTERNAL
                            .withCause(e)
                            .withDescription(e.getMessage())
                            .asException());
                }
            }

            private void closeQuietly() {
                if (fileOut != null) {
                    try {
                        fileOut.close();
                    } catch (IOException ignored) {
                    }
                    fileOut = null;
                }
            }
        };
    }

    @Override
    public void getJobStatus(GetJobStatusRequest request, StreamObserver<GetJobStatusResponse> responseObserver) {
        String jobId = request.getJobId();
        if (jobId == null || jobId.isBlank()) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withDescription("job_id required")
                    .asException());
            return;
        }
        Optional<ThumbnailJob> job = jobService.findById(jobId);
        if (job.isEmpty()) {
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Job not found: " + jobId)
                    .asException());
            return;
        }
        ThumbnailJob j = job.get();
        GetJobStatusResponse response = GetJobStatusResponse.newBuilder()
                .setJobId(j.getJobId())
                .setStatus(j.getStatus().name())
                .setErrorMessage(j.getErrorMessage() != null ? j.getErrorMessage() : "")
                .setThumbnailPath(j.getThumbnailPath() != null ? j.getThumbnailPath() : "")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
