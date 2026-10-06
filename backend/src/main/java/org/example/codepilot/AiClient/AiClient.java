package org.example.codepilot.AiClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockagentcore.BedrockAgentCoreClient;
import software.amazon.awssdk.services.bedrockagentcore.model.InvokeAgentRuntimeResponse;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class AiClient {

    private final String agentcoreRuntimeArn;
    private final BedrockAgentCoreClient agentCoreClient;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public AiClient(
            ObjectMapper objectMapper,
            RestClient.Builder builder,
            @Value("${codepilot.agentcore.runtime-arn}") String agentcoreRuntimeArn,
            @Value("${codepilot.agentcore.region}") String region,
            @Value("${spring.codepilot.ai.base-url}") String fastApiBaseUrl
    ) {
        this.objectMapper = objectMapper;
        this.agentcoreRuntimeArn = agentcoreRuntimeArn;
        this.restClient = builder.baseUrl(fastApiBaseUrl).build();
        this.agentCoreClient = BedrockAgentCoreClient.builder()
                .region(Region.of(region))
                .build();
    }

    public void indexRepository(String repositoryId) {
        restClient.post()
                .uri("/repositories/{repositoryId}/index", repositoryId)
                .retrieve()
                .toBodilessEntity();
    }

    public FastApiResponse ask(String repositoryId, String question, String threadId) {
        try {
            String payloadJson = objectMapper.writeValueAsString(
                    new FastApiRequest(repositoryId, question, threadId)
            );

            ResponseBytes<InvokeAgentRuntimeResponse> response = agentCoreClient.invokeAgentRuntimeAsBytes(
                    request -> request
                            .agentRuntimeArn(agentcoreRuntimeArn)
                            .runtimeSessionId(runtimeSessionId(threadId))
                            .contentType("application/json")
                            .accept("application/json")
                            .payload(SdkBytes.fromUtf8String(payloadJson))
            );


            return objectMapper.readValue(response.asUtf8String(), FastApiResponse.class);
        } catch (Exception exception) {
            throw new RuntimeException("AgentCore request failed", exception);
        }
    }

    private String runtimeSessionId(String threadId) {
        String value = threadId == null || threadId.isBlank()
                ? UUID.randomUUID().toString()
                : threadId;

        if (value.length() >= 33) {
            return value;
        }

        return UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8)).toString();
    }
}
