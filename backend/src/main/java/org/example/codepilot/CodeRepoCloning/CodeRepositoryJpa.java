package org.example.codepilot.CodeRepoCloning;

import org.aspectj.apache.bcel.classfile.Code;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CodeRepositoryJpa extends JpaRepository<CodeRepo, UUID> {

    boolean existsByUrl(String url);
}
