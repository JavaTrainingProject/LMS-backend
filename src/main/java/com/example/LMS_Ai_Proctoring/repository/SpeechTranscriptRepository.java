package com.example.LMS_Ai_Proctoring.repository;

import com.example.LMS_Ai_Proctoring.entity.SpeechTranscript;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpeechTranscriptRepository extends JpaRepository<SpeechTranscript, Long> {
    List<SpeechTranscript> findBySessionIdOrderByDetectedAtAsc(Long sessionId);
}
