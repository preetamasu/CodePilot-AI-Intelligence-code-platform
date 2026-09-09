package org.example.codepilot.CodeRepoCloning;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RequiredArgsConstructor
@RestController
@RequestMapping
        ("/api/v1/code")
public class CodeRepoController {

    private final CodeRepoService codeRepoService;

    @PostMapping("/")
    public ResponseEntity<CodeRepoResponse> create(@RequestBody
                                                   @Valid
                                                   CreateRepoRequest createRepoRequest){
        return new ResponseEntity<>(codeRepoService.create(createRepoRequest), HttpStatus.CREATED);
    }


}
