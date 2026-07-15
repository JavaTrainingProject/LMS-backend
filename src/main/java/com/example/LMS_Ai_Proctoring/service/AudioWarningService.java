package com.example.LMS_Ai_Proctoring.service;

import com.example.LMS_Ai_Proctoring.dto.AudioAnalysisResult;
import com.example.LMS_Ai_Proctoring.entity.ProctoringSessionEntity;
import com.example.LMS_Ai_Proctoring.entity.ProctoringViolationEntity;
import com.example.LMS_Ai_Proctoring.repository.ProctoringSessionRepository;
import com.example.LMS_Ai_Proctoring.repository.ProctoringViolationRepository;
import com.example.LMS_Ai_Proctoring.responseDTO.AudioWarningResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AudioWarningService {

    private final ProctoringSessionRepository sessionRepository;
    private final ProctoringViolationRepository violationRepository;

    private static final int CONSECUTIVE_THRESHOLD = 3;
    private static final int MAX_WARNINGS = 3;

    public AudioWarningResponse processAudio(Long sessionId,
                                             AudioAnalysisResult result) {

        ProctoringSessionEntity session =
                sessionRepository.findById(sessionId)
                        .orElseThrow(() ->
                                new RuntimeException("Session not found"));

        // DEBUG: watch this in your console while you speak.
        // If these numbers never cross your thresholds, adjust AudioConfig.
        System.out.printf(
                "[AUDIO] noise=%.2f%% speech=%.2f%% noiseDetected=%b speechDetected=%b speakers=%d consecNoise=%d consecSpeech=%d%n",
                result.getNoisePercentage(),
                result.getSpeechPercentage(),
                result.isNoiseDetected(),
                result.isSpeechDetected(),
                result.getSpeakerCount(),
                session.getConsecutiveNoiseCount(),
                session.getConsecutiveSpeechCount()
        );

        boolean warningGenerated = false;
        String warningMessage = null;

        // ===========================
        // NOISE
        // ===========================

        if (result.isNoiseDetected()) {

            session.setConsecutiveNoiseCount(
                    session.getConsecutiveNoiseCount() + 1
            );

        } else {

            session.setConsecutiveNoiseCount(0);
        }

        // ===========================
        // SPEECH
        // ===========================

        if (result.isSpeechDetected()) {

            session.setConsecutiveSpeechCount(
                    session.getConsecutiveSpeechCount() + 1
            );

        } else {

            session.setConsecutiveSpeechCount(0);   // FIXED: was never resetting before
        }

        // ===========================
        // MULTIPLE SPEAKERS
        // ===========================

        if (result.getSpeakerCount() > 1) {

            saveViolation(
                    sessionId,
                    "MULTIPLE_SPEAKERS_DETECTED",
                    result
            );

            warningGenerated = true;
            warningMessage = "Multiple speakers detected.";

            session.setAudioWarningCount(
                    session.getAudioWarningCount() + 1
            );
        }

        // ===========================
        // NOISE WARNING
        // ===========================

        if (session.getConsecutiveNoiseCount() >= CONSECUTIVE_THRESHOLD) {

            saveViolation(
                    sessionId,
                    "LOUD_NOISE_DETECTED",
                    result
            );

            session.setAudioWarningCount(
                    session.getAudioWarningCount() + 1
            );

            session.setConsecutiveNoiseCount(0);

            warningGenerated = true;
            warningMessage = "Background noise detected.";
        }

        // ===========================
        // SPEECH WARNING
        // ===========================

        if (session.getConsecutiveSpeechCount() >= CONSECUTIVE_THRESHOLD) {

            saveViolation(
                    sessionId,
                    "SPEECH_DETECTED",
                    result
            );

            session.setAudioWarningCount(
                    session.getAudioWarningCount() + 1
            );

            session.setConsecutiveSpeechCount(0);

            warningGenerated = true;
            warningMessage = "Talking detected.";
        }

        boolean terminate =
                session.getAudioWarningCount() >= MAX_WARNINGS;

        if (terminate) {

            session.setStatus("ENDED");
            session.setEndedAt(LocalDateTime.now());
        }

        sessionRepository.save(session);

        return AudioWarningResponse.builder()
                .noiseDetected(result.isNoiseDetected())
                .speechDetected(result.isSpeechDetected())
                .noisePercentage(result.getNoisePercentage())   // NEW
                .speechPercentage(result.getSpeechPercentage()) // NEW
                .speakerCount(result.getSpeakerCount())
                .warningGenerated(warningGenerated)
                .warningMessage(warningMessage)
                .warningCount(session.getAudioWarningCount())
                .terminateAssessment(terminate)
                .build();
    }

    private void saveViolation(Long sessionId,
                               String eventType,
                               AudioAnalysisResult result) {

        ProctoringViolationEntity entity =
                ProctoringViolationEntity.builder()
                        .sessionId(sessionId)
                        .eventType(eventType)
                        .faceCount(0)
                        .headDirection(null)
                        .consecutiveCount(0)
                        .decibelLevel(result.getNoisePercentage())
                        .speechDetected(result.isSpeechDetected())
                        .speakerCount(result.getSpeakerCount())
                        .detectedAt(LocalDateTime.now())
                        .build();

        violationRepository.save(entity);
    }
}