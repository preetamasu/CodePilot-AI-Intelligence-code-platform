package org.example.codepilot.AiClient;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/repositories")
public class AiController {

    private final AiClient aiClient;

    @PostMapping("/questions")
    public FastApiResponse ask( @RequestBody @Valid PublicDTO publicDTO){
        return aiClient.ask(publicDTO.repo_id(), publicDTO.question(), publicDTO.threadId());
    }

}
