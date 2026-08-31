package org.example.codepilot.AiClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import javax.print.attribute.standard.Media;
import java.net.http.HttpClient;
import java.util.UUID;

@Service
public class AiClient {

    private final RestClient restClient;

    public AiClient(
            RestClient.Builder builder,
            @Value("${spring.codepilot.ai.base-url}") String baseUrl
    ) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        this.restClient = builder
                .baseUrl(baseUrl)
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .build();
    }

    public void indexRepository(String repositoryId)
    {
        restClient.post()
                .uri("/repositories/{repositoryId}/index",repositoryId)
                .retrieve()
                .toBodilessEntity();
    }

    public FastApiResponse ask(String repositoryId, String question) {
        try {
            FastApiRequest request = new FastApiRequest(
                    repositoryId,
                    question
            );

            return restClient.post()
                    .uri("/api/v1/ai/answer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(FastApiResponse.class);

        } catch (RestClientResponseException exception) {
            System.out.println("FastAPI status: " + exception.getStatusCode());
            System.out.println("FastAPI response: " + exception.getResponseBodyAsString());
            throw exception;
        }
    }
}
