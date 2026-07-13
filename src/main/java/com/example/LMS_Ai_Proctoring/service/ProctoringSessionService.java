package com.example.LMS_Ai_Proctoring.service;

import com.example.LMS_Ai_Proctoring.entity.ProctoringSessionEntity;
import com.example.LMS_Ai_Proctoring.enums.ProctoringSessionStatus;
import com.example.LMS_Ai_Proctoring.exception.ProctoringSessionNotActiveException;
import com.example.LMS_Ai_Proctoring.exception.ProctoringSessionNotFoundException;
import com.example.LMS_Ai_Proctoring.repository.ProctoringSessionRepository;
import com.example.LMS_Ai_Proctoring.responseDTO.ProctoringSessionResponse;
import com.example.LMS_Ai_Proctoring.responseDTO.ProctoringSummaryResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

import static com.example.LMS_Ai_Proctoring.enums.ProctoringSessionStatus.ENDED;


@Service
@RequiredArgsConstructor
public class ProctoringSessionService {

    private final ProctoringService proctoringService;

    private final ProctoringViolationService
            proctoringViolationService;

    private final ProctoringSessionRepository
            proctoringSessionRepository;

    private final SpeechToTextService
            speechToTextService;




    // START SESSION

    public ProctoringSessionResponse startSession() {

        ProctoringSessionEntity session =
                ProctoringSessionEntity.builder()
                        .status(String.valueOf(ProctoringSessionStatus.ACTIVE))
                        .startedAt(LocalDateTime.now())
                        .endedAt(null)
                        .totalViolations(0)
                        .lookingLeftCount(0)
                        .lookingRightCount(0)
                        .lookingUpCount(0)
                        .lookingDownCount(0)
                        .noFaceCount(0)
                        .multipleFacesCount(0)
                        .build();


        ProctoringSessionEntity savedSession =
                proctoringSessionRepository.save(
                        session
                );


        return buildResponse(
                savedSession,
                "Proctoring session started successfully"
        );
    }

    // CLOSE SPEECH RECOGNIZER SAFELY

    private void closeRecognizerSafely(
            Long sessionId
    ) {

        try {

            speechToTextService.closeSession(
                    sessionId
            );

        } catch (Exception e) {

            // Log and swallow — session end must still succeed
            // even if recognizer cleanup fails
            e.printStackTrace();
        }
    }



    // VALIDATE ACTIVE SESSION

    public void validateActiveSession(
            Long sessionId
    ) {

        ProctoringSessionEntity session =
                getSessionEntity(
                        sessionId
                );


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

        ProctoringSessionEntity session =
                getSessionEntity(
                        sessionId
                );


        if (!"ACTIVE".equals(
                session.getStatus()
        )) {

            throw new ProctoringSessionNotActiveException(
                    "Proctoring session is not active"
            );
        }


        // GET TOTAL VIOLATION COUNT

        int totalViolations =
                proctoringViolationService
                        .getTotalViolationCount(
                                sessionId
                        );


        // GET EVENT-WISE COUNTS

        Map<String, Long> violationCounts =
                proctoringViolationService
                        .getViolationCounts(
                                sessionId
                        );


        // GET HEAD DIRECTION-WISE COUNTS

        Map<String, Long> headDirectionCounts =
                proctoringViolationService
                        .getHeadDirectionCounts(
                                sessionId
                        );


        // UPDATE SESSION STATUS

        session.setStatus(
                String.valueOf(ProctoringSessionStatus.ENDED)
        );


        session.setEndedAt(
                LocalDateTime.now()
        );


        // UPDATE TOTAL VIOLATIONS

        session.setTotalViolations(
                totalViolations
        );


        // UPDATE LOOKING LEFT COUNT

        session.setLookingLeftCount(
                headDirectionCounts
                        .getOrDefault(
                                "LOOKING_LEFT",
                                0L
                        )
                        .intValue()
        );


        // UPDATE LOOKING RIGHT COUNT

        session.setLookingRightCount(
                headDirectionCounts
                        .getOrDefault(
                                "LOOKING_RIGHT",
                                0L
                        )
                        .intValue()
        );


        // UPDATE LOOKING UP COUNT

        session.setLookingUpCount(
                headDirectionCounts
                        .getOrDefault(
                                "LOOKING_UP",
                                0L
                        )
                        .intValue()
        );


        // UPDATE LOOKING DOWN COUNT

        session.setLookingDownCount(
                headDirectionCounts
                        .getOrDefault(
                                "LOOKING_DOWN",
                                0L
                        )
                        .intValue()
        );


        // UPDATE NO FACE COUNT

        session.setNoFaceCount(
                violationCounts
                        .getOrDefault(
                                "NO_FACE_DETECTED",
                                0L
                        )
                        .intValue()
        );


        // UPDATE MULTIPLE FACES COUNT

        session.setMultipleFacesCount(
                violationCounts
                        .getOrDefault(
                                "MULTIPLE_FACES_DETECTED",
                                0L
                        )
                        .intValue()
        );


        // SAVE FINAL SESSION ANALYSIS

        ProctoringSessionEntity savedSession =
                proctoringSessionRepository.save(
                        session
                );


        // CLEAR ONLY TEMPORARY MONITORING STATE

        proctoringService.clearSession(
                sessionId
        );

        closeRecognizerSafely(sessionId);   //


        return buildResponse(
                savedSession,
                "Proctoring session ended successfully"
        );
    }



    // GET SESSION

    public ProctoringSessionResponse getSession(
            Long sessionId
    ) {

        ProctoringSessionEntity session =
                getSessionEntity(
                        sessionId
                );


        return buildResponse(
                session,
                "Proctoring session fetched successfully"
        );
    }



    // GET SESSION SUMMARY

    public ProctoringSummaryResponse getSessionSummary(
            Long sessionId
    ) {

        ProctoringSessionEntity session =
                getSessionEntity(
                        sessionId
                );


        // GET TOTAL VIOLATION COUNT

        int totalViolations =
                proctoringViolationService
                        .getTotalViolationCount(
                                sessionId
                        );


        // GET EVENT-WISE COUNTS

        Map<String, Long> violationCounts =
                proctoringViolationService
                        .getViolationCounts(
                                sessionId
                        );


        // GET HEAD DIRECTION-WISE COUNTS

        Map<String, Long> headDirectionCounts =
                proctoringViolationService
                        .getHeadDirectionCounts(
                                sessionId
                        );


        violationCounts.put(
                "LOOKING_LEFT",
                headDirectionCounts.getOrDefault(
                        "LOOKING_LEFT",
                        0L
                )
        );


        violationCounts.put(
                "LOOKING_RIGHT",
                headDirectionCounts.getOrDefault(
                        "LOOKING_RIGHT",
                        0L
                )
        );


        violationCounts.put(
                "LOOKING_UP",
                headDirectionCounts.getOrDefault(
                        "LOOKING_UP",
                        0L
                )
        );


        violationCounts.put(
                "LOOKING_DOWN",
                headDirectionCounts.getOrDefault(
                        "LOOKING_DOWN",
                        0L
                )
        );


        return ProctoringSummaryResponse.builder()
                .sessionId(
                        session.getId()
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



    // GET SESSION ENTITY

    private ProctoringSessionEntity getSessionEntity(
            Long sessionId
    ) {

        return proctoringSessionRepository
                .findById(
                        sessionId
                )
                .orElseThrow(
                        () ->
                                new ProctoringSessionNotFoundException(
                                        "Proctoring session not found"
                                )
                );
    }



    // BUILD RESPONSE

    private ProctoringSessionResponse buildResponse(
            ProctoringSessionEntity session,
            String message
    ) {

        return ProctoringSessionResponse.builder()
                .sessionId(
                        session.getId()
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
                .message(
                        message
                )
                .build();
    }
}