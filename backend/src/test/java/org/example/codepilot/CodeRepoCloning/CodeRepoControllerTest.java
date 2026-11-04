package org.example.codepilot.CodeRepoCloning;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CodeRepoControllerTest {
    private CodeRepoService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        service = mock(CodeRepoService.class);
        mvc = MockMvcBuilders.standaloneSetup(new CodeRepoController(service)).build();
    }

    @Test
    void createReturnsCreatedRepository() throws Exception {
        UUID id = UUID.randomUUID();
        String url = "https://github.com/example/demo";
        when(service.create(new CreateRepoRequest(url))).thenReturn(new CodeRepoResponse(
                id, "demo", url, RepositoryStatus.READY, null, Instant.parse("2026-01-01T00:00:00Z")));

        mvc.perform(post("/api/v1/code/").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"" + url + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.url").value(url))
                .andExpect(jsonPath("$.status").value("READY"));
        verify(service).create(new CreateRepoRequest(url));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"url\":\" \"}", "{\"url\":null}", "{broken-json"})
    void invalidRepositoryDoesNotStartCloning(String body) throws Exception {
        mvc.perform(post("/api/v1/code/").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void cloneFailurePreservesServiceErrorStatus() throws Exception {
        when(service.create(any())).thenThrow(new ResponseStatusException(
                HttpStatus.UNPROCESSABLE_CONTENT, "Unable to clone repository"));

        mvc.perform(post("/api/v1/code/").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://github.com/example/missing\"}"))
                .andExpect(status().isUnprocessableContent());
    }
}
