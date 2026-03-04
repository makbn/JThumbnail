package io.github.makbn.jthumbnail.webservice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = WebserviceTestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.main.allow-circular-references=true")
class AdminControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void adminRedirectsToAdminIndex() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/index.html"));
    }
}
