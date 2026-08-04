package org.example.codepilot.CodeRepoCloning;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Data
@Getter
@Setter
@Entity
public class CodeRepo {

    @Id
    @GeneratedValue
            (
                    strategy = GenerationType.AUTO
            )
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 1000)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepositoryStatus status = RepositoryStatus.QUEUED;

    @Column(length = 2000)
    private String errorMessage;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

}
