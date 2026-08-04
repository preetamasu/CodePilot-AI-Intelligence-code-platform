package org.example.codepilot.CodeRepoCloning;

import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CodeRepoService {

    private final CodeRepositoryJpa codeRepositoryJpa;

    public CodeRepoResponse create(CreateRepoRequest createRepoRequest){
        String url = createRepoRequest.url();

        if(codeRepositoryJpa.existsByUrl(url)){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Github repo already exists"
            );
        }

        CodeRepo codeRepo = new CodeRepo();

        codeRepo.setName(extractUserName(url));
        codeRepo.setUrl(createRepoRequest.url());
        codeRepo.setStatus(RepositoryStatus.QUEUED);
        cloneRepository(codeRepo);

        codeRepo = codeRepositoryJpa.save(codeRepo);

        cloneRepository(codeRepo);

        return CodeRepoResponse.repoResponse(codeRepo);

    }
    public CodeRepoResponse findById(UUID id){
        return codeRepositoryJpa.findById(id).stream().map(CodeRepoResponse::repoResponse).
                findAny().orElseThrow(

                        ()-> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Repository not found"

                        ));

    }

    private void cloneRepository(CodeRepo codeRepo){
        Path cloneDirectory = null;

        try{
            codeRepo.setStatus(RepositoryStatus.PARSING);
            codeRepositoryJpa.save(codeRepo);

            cloneDirectory = Files.createTempDirectory(
                    "codepilot" + codeRepo.getId() + "-"
            );

            try(
                    Git ignored = Git.cloneRepository()
                            .setURI(codeRepo.getUrl())
                            .setDirectory(cloneDirectory.toFile())
                            .setDepth(1)
                            .setCloneAllBranches(false)
                            .call()
                    ){
                 codeRepo.setStatus(RepositoryStatus.CLONING);
                 codeRepositoryJpa.save(codeRepo);
            }

            codeRepo.setStatus(RepositoryStatus.READY);
            codeRepo.setErrorMessage(null);
            codeRepositoryJpa.save(codeRepo);

        }
        catch(Exception exception){
            codeRepo.setStatus(RepositoryStatus.FAILED);
            codeRepo.setErrorMessage(exception.getMessage());
            codeRepositoryJpa.save(codeRepo);

            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT,
                    "Unable to clone repository",
                    exception
            );
        }
        finally {
            //deleteDirectory(cloneDirectory);
            System.out.println("Repository cloned at: " + cloneDirectory);
        }
    }

    private String extractUserName(String url) {
        String uri = URI.create(url).getPath();

        String[] urls = uri.substring(1).split("/");

        return urls[0];

    }


}
