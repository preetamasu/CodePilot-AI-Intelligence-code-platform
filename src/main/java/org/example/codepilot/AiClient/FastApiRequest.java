package org.example.codepilot.AiClient;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FastApiRequest(
        @JsonProperty("repo_id") String repositoryId,
        String question,
        @JsonProperty("thread_id") String threadId
) {
}
