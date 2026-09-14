package com.hr.AICareerCopilot.repository;

import com.hr.AICareerCopilot.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, String> {

    Optional<Resume> findTopByOrderByUploadedAtDesc();
}