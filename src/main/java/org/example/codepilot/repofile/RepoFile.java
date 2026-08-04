package org.example.codepilot.repofile;

import jakarta.persistence.*;
import org.example.codepilot.CodeRepoCloning.CodeRepo;

import java.util.UUID;

@Entity
public class RepoFile {
    @Id
    @GeneratedValue
            (
                    strategy = GenerationType.AUTO
            )
    private UUID id;

    @ManyToOne
    @JoinColumn(
            name = "repository_id"
    )
    private CodeRepo repository;

    private

}
