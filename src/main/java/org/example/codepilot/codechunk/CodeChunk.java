package org.example.codepilot.codechunk;

import jakarta.persistence.*;
import lombok.Data;
import org.example.codepilot.CodeRepoCloning.CodeRepo;
import org.example.codepilot.repofile.RepoFile;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
public class CodeChunk {

    @Id
    @GeneratedValue(
            strategy = GenerationType.AUTO
    )
    private UUID id;


    @ManyToOne
    @JoinColumn(
            name = "repo_id"
    )
    private CodeRepo repository;

    @ManyToOne
    @JoinColumn(
            name = "repofile_id"
    )
    private RepoFile repoFile;

    private Integer chunkIndex;

    private String content;

    private Integer  startLine;

    private Instant createdAt;
}
