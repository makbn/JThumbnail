package io.github.makbn.jthumbnail.cdnedge;

import io.github.makbn.jthumbnail.connector.api.ThumbnailJobSubmitter;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CdnEdgeJobControllerTest {

    @Test
    void createJobDownloadsAndSubmits() throws Exception {
        CdnEdgeDownloadService downloader = mock(CdnEdgeDownloadService.class);
        ThumbnailJobSubmitter jobSubmitter = mock(ThumbnailJobSubmitter.class);

        File tmp = File.createTempFile("cdn-edge-test", ".bin");
        when(downloader.downloadToTemp(anyString())).thenReturn(tmp);
        when(jobSubmitter.submit(anyString())).thenReturn("job-123");

        CdnEdgeJobController controller = new CdnEdgeJobController(downloader, jobSubmitter);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();

        String body = """
                {"url":"https://cdn.example.com/path/file.png"}
                """;

        mvc.perform(post("/api/cdnedge/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value("job-123"));

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(jobSubmitter).submit(pathCaptor.capture());
        String submittedPath = pathCaptor.getValue();
        // make sure a non-empty path is passed through
        assertEquals(tmp.getAbsolutePath(), submittedPath);
    }
}

