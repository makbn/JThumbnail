package io.github.makbn.jthumbnail.grpc.server;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.makbn.jthumbnail.connector.api.ThumbnailJobSubmitter;
import io.github.makbn.jthumbnail.core.config.ThumbnailServerConfiguration;
import io.github.makbn.jthumbnail.core.job.ThumbnailJob;
import io.github.makbn.jthumbnail.core.job.ThumbnailJobService;
import io.github.makbn.jthumbnail.core.metrics.ThumbnailMetrics;
import io.github.makbn.jthumbnail.grpc.GetJobStatusRequest;
import io.github.makbn.jthumbnail.grpc.GetJobStatusResponse;
import io.grpc.stub.StreamObserver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ThumbnailGrpcServiceImplTest {

    @Mock
    ThumbnailJobService jobService;

    @Mock
    ThumbnailMetrics metrics;

    @Mock
    ThumbnailServerConfiguration serverConfig;

    @Mock
    ThumbnailJobSubmitter jobSubmitter;

    @Mock
    StreamObserver<GetJobStatusResponse> responseObserver;

    ThumbnailGrpcServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ThumbnailGrpcServiceImpl(jobService, jobSubmitter, metrics, serverConfig);
    }

    @Test
    void getJobStatus_whenJobFound_returnsStatusAndCompletes() {
        ThumbnailJob job = ThumbnailJob.create("/tmp/file.pdf");
        job.setStatus(ThumbnailJob.JobStatus.COMPLETED);
        job.setThumbnailPath("/tmp/thumb.png");
        String jobId = job.getJobId();
        when(jobService.findById(jobId)).thenReturn(Optional.of(job));

        service.getJobStatus(GetJobStatusRequest.newBuilder().setJobId(jobId).build(), responseObserver);

        ArgumentCaptor<GetJobStatusResponse> captor = ArgumentCaptor.forClass(GetJobStatusResponse.class);
        verify(responseObserver).onNext(captor.capture());
        verify(responseObserver).onCompleted();
        GetJobStatusResponse resp = captor.getValue();
        Assertions.assertEquals(jobId, resp.getJobId());
        Assertions.assertEquals("COMPLETED", resp.getStatus());
        Assertions.assertEquals("/tmp/thumb.png", resp.getThumbnailPath());
    }

    @Test
    void getJobStatus_whenJobNotFound_reportsError() {
        when(jobService.findById("missing")).thenReturn(Optional.empty());

        service.getJobStatus(
                GetJobStatusRequest.newBuilder().setJobId("missing").build(), responseObserver);

        verify(responseObserver).onError(any(Throwable.class));
    }
}
