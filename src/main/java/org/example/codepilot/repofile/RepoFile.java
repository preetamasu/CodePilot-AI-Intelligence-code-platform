package org.example.codepilot.repofile;

import jakarta.persistence.*;
import org.example.codepilot.CodeRepoCloning.CodeRepo;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
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

    @Column(nullable = false, length = 2000)
    private String path;

    @Column(length = 50)
    private String language;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Long sizeBytes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

}
