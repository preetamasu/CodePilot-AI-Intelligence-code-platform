package org.example.codepilot.AiClient;

import java.util.List;

public record FastApiResponse(
        String answer,
        List<FastApiSource> sources,
        String thread_id
) {
}
