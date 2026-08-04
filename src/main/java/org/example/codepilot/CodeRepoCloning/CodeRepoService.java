package org.example.codepilot.CodeRepoCloning;

import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.example.codepilot.repofile.RepoFile;
import org.example.codepilot.repofile.RepoFileJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class CodeRepoService {

    private static final long MAX_FILE_SIZE = 1_000_000;

    private static final Set<String> IGNORED_DIRECTORIES = Set.of(
            ".git",
            ".idea",
            ".gradle",
            "node_modules",
            "target",
            "build",
            "dist",
            "out"
    );

    private static final Map<String, String> LANGUAGES = Map.ofEntries(
            Map.entry("java", "Java"),
            Map.entry("py", "Python"),
            Map.entry("js", "JavaScript"),
            Map.entry("jsx", "JavaScript"),
            Map.entry("ts", "TypeScript"),
            Map.entry("tsx", "TypeScript"),
            Map.entry("go", "Go"),
            Map.entry("cs", "C#"),
            Map.entry("c", "C"),
            Map.entry("cpp", "C++"),
            Map.entry("h", "C/C++ Header"),
            Map.entry("html", "HTML"),
            Map.entry("css", "CSS"),
            Map.entry("xml", "XML"),
            Map.entry("json", "JSON"),
            Map.entry("yml", "YAML"),
            Map.entry("yaml", "YAML"),
            Map.entry("properties", "Properties"),
            Map.entry("md", "Markdown"),
            Map.entry("sql", "SQL")
    );


    private final CodeRepositoryJpa codeRepositoryJpa;
    private final RepoFileJpaRepository repoFileJpaRepository;

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
                readAndSaveFiles(cloneDirectory, codeRepo);
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

    private void readAndSaveFiles(Path cloneDirectory, CodeRepo codeRepo) throws Exception{

        try(Stream<Path> paths = Files.walk(cloneDirectory)){
            List<Path> files = paths.filter(Files::isRegularFile)
                    .toList();
            for(Path file: files){
                Path relativePath = cloneDirectory.relativize(file);

                if (isIgnored(relativePath)) {
                    continue;
                }

                long fileSize = Files.size(file);

                String language = detectLanguage(relativePath);

                if(language==null){
                    continue;
                }

                System.out.println(language);

                try{
                    String content = Files.readString(
                            file,
                            StandardCharsets.UTF_8
                    );

                    RepoFile repoFile = new RepoFile();
                    repoFile.setRepository(codeRepo);
                    repoFile.setContent(content);
                    repoFile.setLanguage(language);
                    repoFile.setPath(file.toString());
                    repoFileJpaRepository.save(repoFile);
                }
                catch(Exception exception){
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                }
            }
        }


    }

    private String detectLanguage(Path file) {
        String fileName = file.getFileName().toString().toLowerCase();

        int dotIndex = fileName.lastIndexOf('.');

        if(dotIndex<0 || fileName.length()-1==dotIndex){
            return null;
        }

        String name = fileName.substring(dotIndex+1);

        return LANGUAGES.get(name);

    }

    private boolean isIgnored(Path relativePath) {
        for(Path part: relativePath){
            String name = part.toString().toLowerCase();
            if(IGNORED_DIRECTORIES.contains(name)){
                return true;
            }

        }
        return false;
    }

    private String extractUserName(String url) {
        String uri = URI.create(url).getPath();

        String[] urls = uri.substring(1).split("/");

        return urls[0];

    }


}
