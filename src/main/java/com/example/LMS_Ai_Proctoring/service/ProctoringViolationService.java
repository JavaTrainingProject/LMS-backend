package com.example.LMS_Ai_Proctoring.service;

import com.example.LMS_Ai_Proctoring.dto.FaceAnalysisResult;
import com.example.LMS_Ai_Proctoring.dto.ProctoringViolation;
import com.example.LMS_Ai_Proctoring.responseDTO.ProctoringViolationResponse;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;


@Service
public class ProctoringViolationService {

    /*
     * sessionId -> violations
     *
     * CopyOnWriteArrayList use ki hai
     * because multiple frame requests
     * parallel aa sakti hain.
     */
    private final Map<Long, List<ProctoringViolation>>
            sessionViolations =
            new ConcurrentHashMap<>();


    private final AtomicLong violationIdGenerator =
            new AtomicLong(0);



    // SAVE VIOLATION
    public ProctoringViolation saveViolation(
            Long sessionId,
            String eventType,
            FaceAnalysisResult result
    ) {

        Long violationId =
                violationIdGenerator.incrementAndGet();


        ProctoringViolation violation =
                new ProctoringViolation(
                        violationId,
                        sessionId,
                        eventType,
                        result.getFaceCount(),
                        result.getHeadDirection(),
                        result.getGazeDirection(),
                        LocalDateTime.now()
                );


        sessionViolations
                .computeIfAbsent(
                        sessionId,
                        id -> new CopyOnWriteArrayList<>()
                )
                .add(
                        violation
                );


        return violation;
    }



    // GET ALL VIOLATIONS

    public ProctoringViolationResponse getViolations(
            Long sessionId
    ) {

        List<ProctoringViolation> violations =
                sessionViolations.getOrDefault(
                        sessionId,
                        List.of()
                );


        return ProctoringViolationResponse.builder()
                .sessionId(
                        sessionId
                )
                .totalViolations(
                        violations.size()
                )
                .violations(
                        List.copyOf(violations)
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

        return sessionViolations
                .getOrDefault(
                        sessionId,
                        List.of()
                )
                .size();
    }



    // GET EVENT-WISE VIOLATION COUNTS

    public Map<String, Long> getViolationCounts(
            Long sessionId
    ) {

        List<ProctoringViolation> violations =
                sessionViolations.getOrDefault(
                        sessionId,
                        List.of()
                );


        return violations
                .stream()
                .collect(
                        Collectors.groupingBy(
                                ProctoringViolation::getEventType,
                                Collectors.counting()
                        )
                );
    }
}