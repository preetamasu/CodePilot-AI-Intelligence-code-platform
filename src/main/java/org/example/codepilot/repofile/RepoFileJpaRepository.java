package org.example.codepilot.repofile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RepoFileJpaRepository extends JpaRepository<RepoFile, UUID>
{
    List<RepoFile> findByRepositoryId(UUID id);

    void deleteByRepositoryId(UUID repositoryId);


}
