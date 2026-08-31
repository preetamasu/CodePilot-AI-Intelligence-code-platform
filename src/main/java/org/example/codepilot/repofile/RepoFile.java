package org.example.codepilot.repofile;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.codepilot.CodeRepoCloning.CodeRepo;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Data
@Getter
@AllArgsConstructor
@NoArgsConstructor
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
            name = "repoFile_id"
    )
    private CodeRepo repository;

    @Column(nullable = false, length = 2000)
    private String path;

    @Column(length = 50)
    private String language;


    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Long sizeBytes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

}
