package com.example.LMS_Ai_Proctoring.service;

import com.example.LMS_Ai_Proctoring.dto.FaceAnalysisResult;
import com.example.LMS_Ai_Proctoring.entity.ProctoringViolationEntity;
import com.example.LMS_Ai_Proctoring.repository.ProctoringViolationRepository;
import com.example.LMS_Ai_Proctoring.responseDTO.ProctoringViolationResponse;
import com.example.LMS_Ai_Proctoring.dto.ProctoringViolation;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ProctoringViolationService {

    private final ProctoringViolationRepository
            proctoringViolationRepository;



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