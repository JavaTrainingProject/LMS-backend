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
     * 300 SPEECH_DETECTED entries).
     */
    private final Map<String, LocalDateTime> lastEventTimestamps =
            new ConcurrentHashMap<>();

    private static final long DEBOUNCE_SECONDS = 5;


    // ================= FACE VIOLATIONS (unchanged behaviour) =================

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
        ProctoringViolation violation =
                ProctoringViolation.builder()
                        .violationId(violationId)
                        .sessionId(sessionId)
                        .eventType(eventType)
                        .faceCount(result.getFaceCount())
                        .headDirection(result.getHeadDirection())
                        .gazeDirection(result.getGazeDirection())
                        .detectedAt(LocalDateTime.now())
                        .build();


        ProctoringViolationEntity savedEntity =
                proctoringViolationRepository.save(
                        entity
                );


        return mapToDto(
                savedEntity
        );
    }


    // ================= AUDIO VIOLATIONS (new) =================

    /**
     * Evaluates an AudioAnalysisResult and records violations for
     * noise / speech / multiple-speakers, with debouncing per event type
     * so continuous conditions don't spam the violations list.
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

        Long violationId = violationIdGenerator.incrementAndGet();

        ProctoringViolation violation =
                ProctoringViolation.builder()
                        .violationId(violationId)
                        .sessionId(sessionId)
                        .eventType(eventType)
                        .faceCount(0)                       // n/a for audio events
                        .headDirection(null)                // n/a
                        .gazeDirection(null)                // n/a
                        .speechDetected(result.isSpeechDetected())
                        .speakerCount(result.getSpeakerCount())
                        .detectedAt(now)
                        .build();

        sessionViolations
                .computeIfAbsent(
                        sessionId,
                        id -> new CopyOnWriteArrayList<>()
                )
                .add(violation);
    }


    // ================= SHARED QUERY METHODS (unchanged) =================

    // GET ALL VIOLATIONS
    public ProctoringViolationResponse getViolations(Long sessionId) {

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

                sessionViolations.getOrDefault(sessionId, List.of());

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
                .sessionId(sessionId)
                .totalViolations(violations.size())
                .violations(List.copyOf(violations))
                .message("Proctoring violations fetched successfully")
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
    public int getTotalViolationCount(Long sessionId) {
        return sessionViolations
                .getOrDefault(sessionId, List.of())
                .size();
    }

    // GET EVENT-WISE VIOLATION COUNTS
    public Map<String, Long> getViolationCounts(Long sessionId) {

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
                entity.getDetectedAt()
        );
    }

    public Map<String, Long> getHeadDirectionCounts(
            Long sessionId
    ) {
        List<ProctoringViolation> violations =
                sessionViolations.getOrDefault(sessionId, List.of());

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
                .collect(Collectors.groupingBy(
                        ProctoringViolation::getEventType,
                        Collectors.counting()
                ));
    }
}