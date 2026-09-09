package org.example.codepilot.repofile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface RepoFileJpaRepository extends JpaRepository<RepoFile, UUID>
{
    List<RepoFile> findByRepositoryId(UUID id);

    @Transactional
    void deleteByRepositoryId(UUID repositoryId);


}
