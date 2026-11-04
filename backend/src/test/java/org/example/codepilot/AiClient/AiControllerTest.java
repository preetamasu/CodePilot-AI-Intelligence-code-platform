package org.example.codepilot.AiClient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AiControllerTest {
    private AiClient client;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        client = mock(AiClient.class);
        mvc = MockMvcBuilders.standaloneSetup(new AiController(client)).build();
    }

    @Test
    void questionReturnsAnswerSourcesAndThread() throws Exception {
        when(client.ask("repo-1", "Explain the architecture", "thread-1"))
                .thenReturn(new FastApiResponse("A Spring application",
                        List.of(new FastApiSource("pom.xml", "XML")), "thread-1"));

        mvc.perform(post("/api/v1/repositories/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"repo_id":"repo-1","question":"Explain the architecture","thread_id":"thread-1"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("A Spring application"))
                .andExpect(jsonPath("$.sources[0].path").value("pom.xml"))
                .andExpect(jsonPath("$.sources[0].language").value("XML"))
                .andExpect(jsonPath("$.thread_id").value("thread-1"));
        verify(client).ask("repo-1", "Explain the architecture", "thread-1");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"repo_id\":\"repo-1\",\"question\":\" \",\"thread_id\":\"thread-1\"}",
            "{\"repo_id\":\"repo-1\",\"thread_id\":\"thread-1\"}",
            "{\"repo_id\":\"repo-1\",\"question\":\"Explain\"}",
            "{\"repo_id\":\"repo-1\",\"question\":\"Explain\",\"thread_id\":\" \"}",
            "{broken-json"
    })
    void invalidQuestionDoesNotInvokeAi(String body) throws Exception {
        mvc.perform(post("/api/v1/repositories/questions")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(client);
    }
}
