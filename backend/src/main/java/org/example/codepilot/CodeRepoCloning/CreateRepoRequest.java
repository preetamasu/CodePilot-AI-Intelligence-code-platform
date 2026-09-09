package org.example.codepilot.CodeRepoCloning;

import jakarta.validation.constraints.NotBlank;

public record CreateRepoRequest(
        @NotBlank String url
) {
}
