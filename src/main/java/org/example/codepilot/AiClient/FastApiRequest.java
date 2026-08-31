package org.example.codepilot.AiClient;

import java.util.UUID;

public record FastApiRequest(
        String repo_id,
        String question
) {
}
