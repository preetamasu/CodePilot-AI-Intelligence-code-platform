package org.example.codepilot.AiClient;

import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;

public record PublicDTO(
        String repo_id,
        @NotBlank String question,
        @JsonProperty("thread_id") @NotBlank String threadId
) {
}
