package com.example.LMS_Ai_Proctoring.service;

import com.example.LMS_Ai_Proctoring.dto.FaceAnalysisResult;
import com.example.LMS_Ai_Proctoring.dto.MonitoringState;
import com.example.LMS_Ai_Proctoring.responseDTO.ProctoringFrameResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service
@RequiredArgsConstructor
public class ProctoringService {

    private final FaceDetectionService faceDetectionService;

    private final ProctoringViolationService
            proctoringViolationService;


    // sessionId -> monitoring state
    private final Map<Long, MonitoringState> sessionStates =
            new ConcurrentHashMap<>();


    /*
     * 3 consecutive suspicious frames
     * required to confirm violation.
     */
    private static final int VIOLATION_THRESHOLD = 3;


    /*
     * 2 consecutive NORMAL frames
     * required to confirm recovery.
     */
    private static final int NORMAL_RECOVERY_THRESHOLD = 2;



    // PROCESS FRAME
    public ProctoringFrameResponse processFrame(
            Long sessionId,
            MultipartFile file
    ) throws IOException {



        // 1. VALIDATE SESSION ID
        if (sessionId == null) {

            throw new IllegalArgumentException(
                    "Session ID is required"
            );
        }



        // 2. ANALYZE CURRENT FRAME
        FaceAnalysisResult analysisResult =
                faceDetectionService.analyzeFace(
                        file
                );



        // 3. DETERMINE CURRENT EVENT

        String currentEvent =
                determineEvent(
                        analysisResult
                );


        // 4. GET SESSION STATE

        MonitoringState state =
                sessionStates.computeIfAbsent(
                        sessionId,
                        id -> new MonitoringState()
                );



        // 5. NORMAL FRAME

        if (currentEvent == null) {

            // Record consecutive normal frame
            state.recordNormalFrame();


            int normalRecoveryCount =
                    state.getNormalRecoveryCount();


            // Recovery confirmed
            if (normalRecoveryCount
                    >= NORMAL_RECOVERY_THRESHOLD) {

                state.resetAfterRecovery();


                return buildResponse(
                        sessionId,
                        false,
                        "NORMAL",
                        0,
                        analysisResult,
                        "Student activity is normal"
                );
            }


            /*
             * Only one normal frame received.
             *
             * Do not unlock previous violation yet.
             */
            return buildResponse(
                    sessionId,
                    false,
                    "NORMAL",
                    state.getConsecutiveCount(),
                    analysisResult,
                    "Normal frame detected; waiting for recovery confirmation"
            );
        }



        // 6. RECORD SUSPICIOUS EVENT

        state.recordEvent(
                currentEvent
        );


        int consecutiveCount =
                state.getConsecutiveCount();



        // 7. ALREADY RECORDED / LOCKED

        if (state.isViolationLocked()) {

            return buildResponse(
                    sessionId,
                    false,
                    currentEvent,
                    consecutiveCount,
                    analysisResult,
                    "Violation already recorded; waiting for normal recovery"
            );
        }



        // 8. CHECK VIOLATION THRESHOLD

        boolean violationConfirmed =
                consecutiveCount
                        >= VIOLATION_THRESHOLD;



        // 9. CONFIRMED VIOLATION

        if (violationConfirmed) {


            // Save violation only once
            proctoringViolationService.saveViolation(
                    sessionId,
                    currentEvent,
                    analysisResult
            );


            // Lock same continuous event
            state.lockViolation();


            return buildResponse(
                    sessionId,
                    true,
                    currentEvent,
                    consecutiveCount,
                    analysisResult,
                    "Violation confirmed and recorded"
            );
        }



        // 10. SUSPICIOUS BUT NOT CONFIRMED
        return buildResponse(
                sessionId,
                false,
                currentEvent,
                consecutiveCount,
                analysisResult,
                "Suspicious activity detected"
        );
    }


    // DETERMINE EVENT

    private String determineEvent(
            FaceAnalysisResult result
    ) {


        // NO FACE
        if (result.getFaceCount() == 0) {

            return "NO_FACE_DETECTED";
        }


        // MULTIPLE FACES
        if (result.getFaceCount() > 1) {

            return "MULTIPLE_FACES_DETECTED";
        }


        // HEAD LOOKING AWAY
        if (
                "LOOKING_LEFT".equals(
                        result.getHeadDirection()
                )
                        ||
                        "LOOKING_RIGHT".equals(
                                result.getHeadDirection()
                        )
        ) {

            return "LOOKING_AWAY";
        }


        /*
         * IMPORTANT:
         *
         * GAZE_LEFT / GAZE_RIGHT intentionally
         * violation decision me use nahi kar rahe.
         *
         * Current gaze algorithm basic heuristic hai
         * aur false positives de raha tha.
         */


        // NORMAL
        return null;
    }


    // BUILD RESPONSE

    private ProctoringFrameResponse buildResponse(
            Long sessionId,
            boolean violationConfirmed,
            String eventType,
            int consecutiveCount,
            FaceAnalysisResult result,
            String message
    ) {

        return ProctoringFrameResponse.builder()
                .sessionId(
                        sessionId
                )
                .violationConfirmed(
                        violationConfirmed
                )
                .eventType(
                        eventType
                )
                .consecutiveCount(
                        consecutiveCount
                )
                .faceCount(
                        result.getFaceCount()
                )
                .headDirection(
                        result.getHeadDirection()
                )
                .gazeDirection(
                        result.getGazeDirection()
                )
                .message(
                        message
                )
                .build();
    }


    // CLEAR SESSION STATE

    public void clearSession(
            Long sessionId
    ) {

        sessionStates.remove(
                sessionId
        );
    }
}