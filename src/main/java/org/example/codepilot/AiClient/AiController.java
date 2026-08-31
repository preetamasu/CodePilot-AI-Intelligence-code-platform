package org.example.codepilot.AiClient;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/repositories")
public class AiController {

    private final AiClient aiClient;

    @PostMapping("/{repositoryId}/questions")
    public FastApiResponse ask(@PathVariable String repositoryId, @RequestBody @Valid PublicDTO publicDTO){
        return aiClient.ask(repositoryId,publicDTO.question());
    }

}
