package com.example.LMS_Ai_Proctoring.service;

import com.example.LMS_Ai_Proctoring.dto.ProctoringSession;
import com.example.LMS_Ai_Proctoring.exception.ProctoringSessionNotActiveException;
import com.example.LMS_Ai_Proctoring.exception.ProctoringSessionNotFoundException;
import com.example.LMS_Ai_Proctoring.responseDTO.ProctoringSessionResponse;

import com.example.LMS_Ai_Proctoring.responseDTO.ProctoringSummaryResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


@Service
@RequiredArgsConstructor
public class ProctoringSessionService {

    private final ProctoringService proctoringService;

    private final ProctoringViolationService proctoringViolationService;


    // IN-MEMORY SESSION STORAGE
    private final Map<Long, ProctoringSession> sessions =
            new ConcurrentHashMap<>();



    // SESSION ID GENERATOR

    private final AtomicLong sessionIdGenerator =
            new AtomicLong(1000);



    // START SESSION

    public ProctoringSessionResponse startSession() {

        Long sessionId =
                sessionIdGenerator.incrementAndGet();


        ProctoringSession session =
                new ProctoringSession(
                        sessionId,
                        "ACTIVE",
                        LocalDateTime.now(),
                        null
                );


        sessions.put(
                sessionId,
                session
        );


        return buildResponse(
                session,
                "Proctoring session started successfully"
        );
    }



    // VALIDATE ACTIVE SESSION
    public void validateActiveSession(
            Long sessionId
    ) {

        ProctoringSession session =
                sessions.get(sessionId);


        // SESSION NOT FOUND
        if (session == null) {

            throw new ProctoringSessionNotFoundException(
                    "Proctoring session not found"
            );
        }


        // SESSION NOT ACTIVE
        if (!"ACTIVE".equals(
                session.getStatus()
        )) {

            throw new ProctoringSessionNotActiveException(
                    "Proctoring session is not active"
            );
        }
    }



    // END SESSION

    public ProctoringSessionResponse endSession(
            Long sessionId
    ) {

        ProctoringSession session =
                sessions.get(sessionId);


        // SESSION NOT FOUND
        if (session == null) {

            throw new ProctoringSessionNotFoundException(
                    "Proctoring session not found"
            );
        }


        // SESSION ALREADY ENDED / NOT ACTIVE
        if (!"ACTIVE".equals(
                session.getStatus()
        )) {

            throw new ProctoringSessionNotActiveException(
                    "Proctoring session is not active"
            );
        }


        // UPDATE STATUS
        session.setStatus(
                "ENDED"
        );


        // SET END TIME
        session.setEndedAt(
                LocalDateTime.now()
        );


        // Clear consecutive-frame monitoring state
        proctoringService.clearSession(
                sessionId
        );


        return buildResponse(
                session,
                "Proctoring session ended successfully"
        );
    }


    // GET SESSION

    public ProctoringSessionResponse getSession(
            Long sessionId
    ) {

        ProctoringSession session =
                sessions.get(sessionId);


        // SESSION NOT FOUND
        if (session == null) {

            throw new ProctoringSessionNotFoundException(
                    "Proctoring session not found"
            );
        }


        return buildResponse(
                session,
                "Proctoring session fetched successfully"
        );
    }



    // BUILD RESPONSE

    private ProctoringSessionResponse buildResponse(
            ProctoringSession session,
            String message
    ) {

        return ProctoringSessionResponse.builder()
                .sessionId(
                        session.getSessionId()
                )
                .status(
                        session.getStatus()
                )
                .startedAt(
                        session.getStartedAt()
                )
                .endedAt(
                        session.getEndedAt()
                )
                .message(message)
                .build();
    }


// GET SESSION SUMMARY

    public ProctoringSummaryResponse getSessionSummary(
            Long sessionId
    ) {

        ProctoringSession session =
                sessions.get(sessionId);


        if (session == null) {

            throw new ProctoringSessionNotFoundException(
                    "Proctoring session not found"
            );
        }


        int totalViolations =
                proctoringViolationService
                        .getTotalViolationCount(
                                sessionId
                        );


        Map<String, Long> violationCounts =
                proctoringViolationService
                        .getViolationCounts(
                                sessionId
                        );


        return ProctoringSummaryResponse.builder()
                .sessionId(
                        session.getSessionId()
                )
                .status(
                        session.getStatus()
                )
                .startedAt(
                        session.getStartedAt()
                )
                .endedAt(
                        session.getEndedAt()
                )
                .totalViolations(
                        totalViolations
                )
                .violationCounts(
                        violationCounts
                )
                .message(
                        "Proctoring summary fetched successfully"
                )
                .build();
    }
}