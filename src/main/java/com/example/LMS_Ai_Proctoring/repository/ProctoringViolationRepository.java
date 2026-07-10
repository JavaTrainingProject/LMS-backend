package com.example.LMS_Ai_Proctoring.repository;

import com.example.LMS_Ai_Proctoring.entity.ProctoringViolationEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ProctoringViolationRepository
        extends JpaRepository<ProctoringViolationEntity, Long> {


    // GET ALL VIOLATIONS BY SESSION ID

    List<ProctoringViolationEntity> findBySessionId(
            Long sessionId
    );


    // COUNT ALL VIOLATIONS BY SESSION ID

    long countBySessionId(
            Long sessionId
    );
}