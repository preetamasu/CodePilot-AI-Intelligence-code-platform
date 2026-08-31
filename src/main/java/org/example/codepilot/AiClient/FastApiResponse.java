package org.example.codepilot.AiClient;

import java.util.List;

public record FastApiResponse(
        String answer,
        List<String> sources
) {
}
