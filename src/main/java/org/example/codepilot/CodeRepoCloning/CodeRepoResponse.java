package org.example.codepilot.CodeRepoCloning;

import java.time.Instant;
import java.util.UUID;

public record CodeRepoResponse(UUID id,
                               String name,
                               String url,
                               RepositoryStatus status,
                               String errorMessage,
                               Instant createdAt) {

    public static CodeRepoResponse repoResponse(CodeRepo codeRepo){

        return new CodeRepoResponse(
                codeRepo.getId(),
                codeRepo.getName(),
                codeRepo.getUrl(),
                codeRepo.getStatus(),
                codeRepo.getErrorMessage(),
                codeRepo.getCreatedAt()
        );
    }
}
