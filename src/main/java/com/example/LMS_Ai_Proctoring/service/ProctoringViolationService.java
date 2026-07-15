package com.example.LMS_Ai_Proctoring.service;

import com.example.LMS_Ai_Proctoring.dto.AudioAnalysisResult;
import com.example.LMS_Ai_Proctoring.dto.FaceAnalysisResult;
import com.example.LMS_Ai_Proctoring.entity.ProctoringViolationEntity;
import com.example.LMS_Ai_Proctoring.repository.ProctoringViolationRepository;
import com.example.LMS_Ai_Proctoring.responseDTO.ProctoringViolationResponse;
import com.example.LMS_Ai_Proctoring.dto.ProctoringViolation;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ProctoringViolationService {

    private final ProctoringViolationRepository
            proctoringViolationRepository;


    /*
     * Debounce map: sessionId + eventType -> last time it was recorded.
     * Prevents flooding when noise/speech is continuous
     * (e.g. someone talking for 30 seconds shouldn't create
     * 300 SPEECH_DETECTED rows in the DB).
     *
     * This stays in-memory (per instance) since it's just a
     * rate-limiting guard, not persisted data.
     */
    private final Map<String, LocalDateTime> lastEventTimestamps =
            new ConcurrentHashMap<>();

    private static final long DEBOUNCE_SECONDS = 5;


    // ================= FACE VIOLATIONS (unchanged) =================

    // SAVE VIOLATION

    public ProctoringViolation saveViolation(
            Long sessionId,
            String eventType,
            FaceAnalysisResult result,
            int consecutiveCount
    ) {

        String headDirection = null;


        if ("LOOKING_AWAY".equals(eventType)) {

            headDirection =
                    result.getHeadDirection();
        }


        ProctoringViolationEntity entity =
                ProctoringViolationEntity.builder()
                        .sessionId(sessionId)
                        .eventType(eventType)
                        .faceCount(
                                result.getFaceCount()
                        )
                        .headDirection(
                                headDirection
                        )
                        .consecutiveCount(
                                consecutiveCount
                        )
                        .detectedAt(
                                LocalDateTime.now()
                        )
                        .build();


        ProctoringViolationEntity savedEntity =
                proctoringViolationRepository.save(
                        entity
                );


        return mapToDto(
                savedEntity
        );
    }


    // ================= AUDIO VIOLATIONS (new, DB-backed) =================

    /**
     * Evaluates an AudioAnalysisResult and persists violations for
     * noise / speech / multiple-speakers, with debouncing per event type
     * so continuous conditions don't spam the violations table.
     */
    public void evaluateAudioResult(Long sessionId, AudioAnalysisResult result) {

        if (Boolean.TRUE.equals(result.isNoiseDetected())) {
            recordAudioViolationIfNotDebounced(
                    sessionId,
                    "LOUD_NOISE_DETECTED",
                    result
            );
        }

        if (Boolean.TRUE.equals(result.isSpeechDetected())) {
            recordAudioViolationIfNotDebounced(
                    sessionId,
                    "SPEECH_DETECTED",
                    result
            );
        }

        if (result.getSpeakerCount() > 1) {
            recordAudioViolationIfNotDebounced(
                    sessionId,
                    "MULTIPLE_SPEAKERS_DETECTED",
                    result
            );
        }
    }


    private void recordAudioViolationIfNotDebounced(
            Long sessionId,
            String eventType,
            AudioAnalysisResult result
    ) {

        String debounceKey = sessionId + ":" + eventType;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastTime = lastEventTimestamps.get(debounceKey);

        if (lastTime != null
                && ChronoUnit.SECONDS.between(lastTime, now) < DEBOUNCE_SECONDS) {
            return; // too soon since last identical event, skip
        }

        lastEventTimestamps.put(debounceKey, now);

        ProctoringViolationEntity entity =
                ProctoringViolationEntity.builder()
                        .sessionId(sessionId)
                        .eventType(eventType)
                        .faceCount(0)                       // n/a for audio events
                        .headDirection(null)                // n/a
                        .consecutiveCount(0)                // n/a for audio events
                        .decibelLevel(result.getNoisePercentage())
                        .speechDetected(result.isSpeechDetected())
                        .speakerCount(result.getSpeakerCount())
                        .detectedAt(now)
                        .build();

        proctoringViolationRepository.save(entity);
    }


    // ================= SHARED QUERY METHODS (unchanged) =================

    // GET ALL VIOLATIONS

    public ProctoringViolationResponse getViolations(
            Long sessionId
    ) {

        List<ProctoringViolation> violations =
                proctoringViolationRepository
                        .findBySessionId(
                                sessionId
                        )
                        .stream()
                        .map(
                                this::mapToDto
                        )
                        .toList();


        return ProctoringViolationResponse.builder()
                .sessionId(
                        sessionId
                )
                .totalViolations(
                        violations.size()
                )
                .violations(
                        violations
                )
                .message(
                        "Proctoring violations fetched successfully"
                )
                .build();
    }



    // GET TOTAL VIOLATION COUNT

    public int getTotalViolationCount(
            Long sessionId
    ) {

        return Math.toIntExact(
                proctoringViolationRepository
                        .countBySessionId(
                                sessionId
                        )
        );
    }



    // GET EVENT-WISE VIOLATION COUNTS

    public Map<String, Long> getViolationCounts(
            Long sessionId
    ) {

        return proctoringViolationRepository
                .findBySessionId(
                        sessionId
                )
                .stream()
                .collect(
                        Collectors.groupingBy(
                                ProctoringViolationEntity::getEventType,
                                Collectors.counting()
                        )
                );
    }



    // MAP ENTITY TO DTO

    private ProctoringViolation mapToDto(
            ProctoringViolationEntity entity
    ) {

        return new ProctoringViolation(
                entity.getId(),
                entity.getSessionId(),
                entity.getEventType(),
                entity.getFaceCount(),
                entity.getHeadDirection(),
                entity.getConsecutiveCount(),
                entity.getDecibelLevel(),
                entity.getSpeechDetected(),
                entity.getSpeakerCount(),
                entity.getDetectedAt()
        );
    }

    public Map<String, Long> getHeadDirectionCounts(
            Long sessionId
    ) {

        return proctoringViolationRepository
                .findBySessionId(
                        sessionId
                )
                .stream()
                .filter(
                        violation ->
                                violation.getHeadDirection()
                                        != null
                )
                .collect(
                        Collectors.groupingBy(
                                ProctoringViolationEntity::getHeadDirection,
                                Collectors.counting()
                        )
                );
    }
}