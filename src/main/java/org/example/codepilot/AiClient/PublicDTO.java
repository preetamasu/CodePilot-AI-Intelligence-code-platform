package org.example.codepilot.AiClient;

import jakarta.validation.constraints.NotBlank;

public record PublicDTO(@NotBlank String question) {
}
