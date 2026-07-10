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

    // Session monitoring states
    private final Map<Long, MonitoringState> sessionStates =
            new ConcurrentHashMap<>();

    // Frames required to confirm violation
    private static final int VIOLATION_THRESHOLD = 3;

    // Normal frames required for recovery
    private static final int NORMAL_RECOVERY_THRESHOLD = 2;


    // Process frame
    public ProctoringFrameResponse processFrame(
            Long sessionId,
            MultipartFile file
    ) throws IOException {

        // Validate session
        if (sessionId == null) {
            throw new IllegalArgumentException(
                    "Session ID is required"
            );
        }

        // Analyze frame
        FaceAnalysisResult analysisResult =
                faceDetectionService.analyzeFace(
                        file
                );

        // Determine event
        String currentEvent =
                determineEvent(
                        analysisResult
                );

        // Get monitoring state
        MonitoringState state =
                sessionStates.computeIfAbsent(
                        sessionId,
                        id -> new MonitoringState()
                );

        // Handle normal frame
        if (currentEvent == null) {

            state.recordNormalFrame();

            int normalRecoveryCount =
                    state.getNormalRecoveryCount();

            // Confirm recovery
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

            return buildResponse(
                    sessionId,
                    false,
                    "NORMAL",
                    state.getConsecutiveCount(),
                    analysisResult,
                    "Normal frame detected; waiting for recovery confirmation"
            );
        }

        // Record suspicious event
        state.recordEvent(
                currentEvent,
                getEventDirection(
                        currentEvent,
                        analysisResult
                )
        );

        int consecutiveCount =
                state.getConsecutiveCount();

        // Check locked violation
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

        // Check violation threshold
        boolean violationConfirmed =
                consecutiveCount
                        >= VIOLATION_THRESHOLD;

        // Save confirmed violation
        if (violationConfirmed) {

            proctoringViolationService.saveViolation(
                    sessionId,
                    currentEvent,
                    analysisResult,
                    consecutiveCount
            );

            state.resetAfterViolation();

            return buildResponse(
                    sessionId,
                    true,
                    currentEvent,
                    consecutiveCount,
                    analysisResult,
                    "Violation confirmed and recorded"
            );
        }

        // Suspicious event
        return buildResponse(
                sessionId,
                false,
                currentEvent,
                consecutiveCount,
                analysisResult,
                "Suspicious activity detected"
        );
    }


    // Determine event
    private String determineEvent(
            FaceAnalysisResult result
    ) {

        if (result.getFaceCount() == 0) {
            return "NO_FACE_DETECTED";
        }

        if (result.getFaceCount() > 1) {
            return "MULTIPLE_FACES_DETECTED";
        }

        if ("LOOKING_LEFT".equals(
                result.getHeadDirection()
        )) {
            return "LOOKING_AWAY";
        }

        if ("LOOKING_RIGHT".equals(
                result.getHeadDirection()
        )) {
            return "LOOKING_AWAY";
        }

        if ("LOOKING_UP".equals(
                result.getHeadDirection()
        )) {
            return "LOOKING_AWAY";
        }

        if ("LOOKING_DOWN".equals(
                result.getHeadDirection()
        )) {
            return "LOOKING_AWAY";
        }

        return null;
    }


    // Get event direction
    private String getEventDirection(
            String currentEvent,
            FaceAnalysisResult result
    ) {

        if ("LOOKING_AWAY".equals(
                currentEvent
        )) {
            return result.getHeadDirection();
        }

        return null;
    }


    // Build response
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
                .message(
                        message
                )
                .build();
    }


    // Clear session state
    public void clearSession(
            Long sessionId
    ) {

        sessionStates.remove(
                sessionId
        );
    }
}