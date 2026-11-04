package org.example.codepilot.CodeRepoCloning;

import org.example.codepilot.AiClient.AiClient;
import org.example.codepilot.repofile.RepoFileJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CodeRepoServiceTest {
    @Mock AiClient aiClient;
    @Mock CodeRepositoryJpa repositories;
    @Mock RepoFileJpaRepository files;
    private CodeRepoService service;

    @BeforeEach
    void setUp() {
        service = new CodeRepoService(aiClient, repositories, files);
    }

    @ParameterizedTest
    @EnumSource(value = RepositoryStatus.class, names = {"QUEUED", "CLONING", "PARSING", "INDEXING", "READY"})
    void existingRepositoryIsReturnedWithoutReindexing(RepositoryStatus status) {
        CodeRepo repository = repository(status);
        when(repositories.findByUrl(repository.getUrl())).thenReturn(Optional.of(repository));

        CodeRepoResponse response = service.create(new CreateRepoRequest(repository.getUrl()));

        assertThat(response.id()).isEqualTo(repository.getId());
        assertThat(response.status()).isEqualTo(status);
        verify(repositories, never()).save(any());
        verifyNoInteractions(aiClient, files);
    }

    @Test
    void findByIdReturnsRepositoryDetails() {
        CodeRepo repository = repository(RepositoryStatus.READY);
        when(repositories.findById(repository.getId())).thenReturn(Optional.of(repository));

        CodeRepoResponse response = service.findById(repository.getId());

        assertThat(response).isEqualTo(new CodeRepoResponse(repository.getId(), "demo",
                repository.getUrl(), RepositoryStatus.READY, null, repository.getCreatedAt()));
        verifyNoInteractions(aiClient, files);
    }

    @Test
    void missingRepositoryReturnsNotFound() {
        UUID id = UUID.randomUUID();
        when(repositories.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception -> {
                    assertThat(exception.getStatusCode().value()).isEqualTo(404);
                    assertThat(exception.getReason()).isEqualTo("Repository not found");
                });
        verifyNoInteractions(aiClient, files);
    }

    private CodeRepo repository(RepositoryStatus status) {
        CodeRepo repository = new CodeRepo();
        repository.setId(UUID.randomUUID());
        repository.setName("demo");
        repository.setUrl("https://github.com/example/demo");
        repository.setStatus(status);
        repository.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        return repository;
    }
}
