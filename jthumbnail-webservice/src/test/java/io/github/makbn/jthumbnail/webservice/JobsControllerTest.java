package io.github.makbn.jthumbnail.webservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.makbn.jthumbnail.core.job.ThumbnailJob;
import io.github.makbn.jthumbnail.core.job.ThumbnailJobService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = io.github.makbn.jthumbnail.webservice.WebserviceTestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.main.allow-circular-references=true")
@Transactional
class JobsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ThumbnailJobService jobService;

    @Test
    void getJobReturns404WhenNotFound() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/jobs/non-existent-id"))
                .andReturn();
        assertEquals(HttpStatus.NOT_FOUND.value(), result.getResponse().getStatus());
    }

    @Test
    void getJobReturnsJobWhenFound() throws Exception {
        ThumbnailJob job = jobService.createJob("/tmp/some/file.pdf");
        jobService.markCompleted(job.getJobId(), "/tmp/out/thumb.gif");

        var result = mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + job.getJobId()))
                .andReturn();
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        String body = result.getResponse().getContentAsString();
        assertNotNull(body);
        assertTrue(body.contains(job.getJobId()));
        assertTrue(body.contains("COMPLETED"));
    }

    @Test
    void listJobsByStatusReturnsFilteredList() throws Exception {
        ThumbnailJob failed = jobService.createJob("/tmp/fail.pdf");
        jobService.markFailed(failed.getJobId(), "Test error");

        var result = mockMvc.perform(MockMvcRequestBuilders.get("/jobs").param("status", "FAILED"))
                .andReturn();
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        String body = result.getResponse().getContentAsString();
        assertTrue(body.contains("FAILED") && body.contains(failed.getJobId()));
    }

    @Test
    void listJobsWithoutStatusReturnsAll() throws Exception {
        jobService.createJob("/tmp/a.pdf");
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/jobs")).andReturn();
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertTrue(result.getResponse().getContentAsString().contains("result"));
    }

    @Test
    void listJobsWithInvalidStatusReturns400() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/jobs").param("status", "INVALID"))
                .andReturn();
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }

    @Test
    void retryJobReturns400WhenJobNotFailed() throws Exception {
        ThumbnailJob job = jobService.createJob("/tmp/pending.pdf");
        var result = mockMvc.perform(MockMvcRequestBuilders.post("/jobs/" + job.getJobId() + "/retry"))
                .andReturn();
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
        assertTrue(result.getResponse().getContentAsString().contains("Only FAILED"));
    }

    @Test
    void retryJobReturns404WhenJobNotFound() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.post("/jobs/non-existent-id/retry"))
                .andReturn();
        assertEquals(HttpStatus.NOT_FOUND.value(), result.getResponse().getStatus());
    }

    @Test
    void getThumbnailImageReturns404WhenJobNotFound() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/jobs/non-existent-id/thumbnail"))
                .andReturn();
        assertEquals(HttpStatus.NOT_FOUND.value(), result.getResponse().getStatus());
    }

    @Test
    void getThumbnailImageReturns404WhenJobNotCompleted() throws Exception {
        ThumbnailJob job = jobService.createJob("/tmp/file.pdf");
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + job.getJobId() + "/thumbnail"))
                .andReturn();
        assertEquals(HttpStatus.NOT_FOUND.value(), result.getResponse().getStatus());
    }

    @Test
    void getThumbnailImageReturns200WhenCompletedAndFileExists() throws Exception {
        java.nio.file.Path thumbPath = java.nio.file.Files.createTempFile("thumb", ".png");
        java.nio.file.Files.write(thumbPath, new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47});
        ThumbnailJob job = jobService.createJob("/tmp/source.png");
        jobService.markCompleted(job.getJobId(), thumbPath.toAbsolutePath().toString());
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + job.getJobId() + "/thumbnail"))
                .andReturn();
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertTrue(result.getResponse().getContentAsByteArray().length > 0);
    }
}
