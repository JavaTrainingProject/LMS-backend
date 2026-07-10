package com.example.LMS_Ai_Proctoring.repository;

import com.example.LMS_Ai_Proctoring.entity.ProctoringSessionEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ProctoringSessionRepository
        extends JpaRepository<ProctoringSessionEntity, Long> {
}