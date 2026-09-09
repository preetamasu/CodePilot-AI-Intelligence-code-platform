package org.example.codepilot.AiClient;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AiControllerTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory()
            .getValidator();

    @Test
    void forwardsThreadIdToAiClient() {
        RecordingAiClient aiClient = new RecordingAiClient();
        AiController controller = new AiController(aiClient);

        controller.ask("repository-123", new PublicDTO("What does this do?", "thread-456"));

        assertEquals("repository-123", aiClient.repositoryId);
        assertEquals("What does this do?", aiClient.question);
        assertEquals("thread-456", aiClient.threadId);
    }

    @Test
    void rejectsBlankThreadId() {
        var violations = validator.validate(new PublicDTO("What does this do?", " "));

        assertFalse(violations.isEmpty());
    }

    private static final class RecordingAiClient extends AiClient {

        private String repositoryId;
        private String question;
        private String threadId;

        private RecordingAiClient() {
            super(RestClient.builder(), "http://localhost");
        }

        @Override
        public FastApiResponse ask(String repositoryId, String question, String threadId) {
            this.repositoryId = repositoryId;
            this.question = question;
            this.threadId = threadId;
            return new FastApiResponse("answer", null);
        }
    }
}
