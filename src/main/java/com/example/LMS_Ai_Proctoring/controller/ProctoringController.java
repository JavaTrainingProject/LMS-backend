package com.example.LMS_Ai_Proctoring.controller;

import com.example.LMS_Ai_Proctoring.dto.FaceAnalysisResult;
import com.example.LMS_Ai_Proctoring.enums.ProctoringEventType;
import com.example.LMS_Ai_Proctoring.responseDTO.*;
import com.example.LMS_Ai_Proctoring.service.FaceDetectionService;
import com.example.LMS_Ai_Proctoring.service.ProctoringService;
import com.example.LMS_Ai_Proctoring.service.ProctoringSessionService;
import com.example.LMS_Ai_Proctoring.service.ProctoringViolationService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
@RequestMapping("/api/v1/proctoring")
@RequiredArgsConstructor
public class ProctoringController {

    private final FaceDetectionService faceDetectionService;

    private final ProctoringService proctoringService;

    private final ProctoringSessionService proctoringSessionService;

    private final ProctoringViolationService proctoringViolationService;



    // 1. SINGLE IMAGE TEST API

    @PostMapping(
            value = "/detect",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<FaceDetectionResponse> detectFace(
            @RequestPart("file") MultipartFile file
    ) throws IOException {

        FaceAnalysisResult result =
                faceDetectionService.analyzeFace(file);

        FaceDetectionResponse response;



        // NO FACE

        if (result.getFaceCount() == 0) {

            response = FaceDetectionResponse.builder()
                    .faceCount(0)
                    .eventType(
                            ProctoringEventType.NO_FACE_DETECTED
                    )
                    .violation(true)
                    .message(
                            "No face detected"
                    )
                    .build();
        }



        // MULTIPLE FACES

        else if (result.getFaceCount() > 1) {

            response = FaceDetectionResponse.builder()
                    .faceCount(
                            result.getFaceCount()
                    )
                    .eventType(
                            ProctoringEventType
                                    .MULTIPLE_FACES_DETECTED
                    )
                    .violation(true)
                    .message(
                            "Multiple faces detected"
                    )
                    .build();
        }



        // HEAD LOOKING AWAY
        else if (
                "LOOKING_LEFT".equals(
                        result.getHeadDirection()
                )
                        ||
                        "LOOKING_RIGHT".equals(
                                result.getHeadDirection()
                        )
        ) {

            response = FaceDetectionResponse.builder()
                    .faceCount(1)
                    .eventType(
                            ProctoringEventType.LOOKING_AWAY
                    )
                    .violation(true)
                    .message(
                            "Student head is turned away"
                    )
                    .build();
        }


        // EYES LOOKING AWAY

        else if (
                "GAZE_LEFT".equals(
                        result.getGazeDirection()
                )
                        ||
                        "GAZE_RIGHT".equals(
                                result.getGazeDirection()
                        )
        ) {

            response = FaceDetectionResponse.builder()
                    .faceCount(1)
                    .eventType(
                            ProctoringEventType.LOOKING_AWAY
                    )
                    .violation(true)
                    .message(
                            "Student eyes are looking away"
                    )
                    .build();
        }



        // NORMAL

        else {

            response = FaceDetectionResponse.builder()
                    .faceCount(1)
                    .eventType(
                            ProctoringEventType
                                    .SINGLE_FACE_DETECTED
                    )
                    .violation(false)
                    .message(
                            "Student is looking forward"
                    )
                    .build();
        }


        return ResponseEntity.ok(
                response
        );
    }


    // 2. START PROCTORING SESSION

    @PostMapping("/sessions/start")
    public ResponseEntity<ProctoringSessionResponse>
    startSession() {

        ProctoringSessionResponse response =
                proctoringSessionService.startSession();

        return ResponseEntity.ok(
                response
        );
    }


    // 3. GET PROCTORING SESSION

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ProctoringSessionResponse>
    getSession(
            @PathVariable Long sessionId
    ) {

        ProctoringSessionResponse response =
                proctoringSessionService.getSession(
                        sessionId
                );

        return ResponseEntity.ok(
                response
        );
    }



    // 4. CONTINUOUS MONITORING FRAME API

    @PostMapping(
            value = "/sessions/{sessionId}/frames",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ProctoringFrameResponse>
    processFrame(
            @PathVariable Long sessionId,
            @RequestPart("file") MultipartFile file
    ) throws IOException {


        // Frame sirf ACTIVE session me process hoga
        proctoringSessionService.validateActiveSession(
                sessionId
        );


        ProctoringFrameResponse response =
                proctoringService.processFrame(
                        sessionId,
                        file
                );


        return ResponseEntity.ok(
                response
        );
    }



    // 5. GET SESSION VIOLATIONS

    @GetMapping(
            "/sessions/{sessionId}/violations"
    )
    public ResponseEntity<ProctoringViolationResponse>
    getViolations(
            @PathVariable Long sessionId
    ) {


        // First check session exists or not
        proctoringSessionService.getSession(
                sessionId
        );


        ProctoringViolationResponse response =
                proctoringViolationService.getViolations(
                        sessionId
                );


        return ResponseEntity.ok(
                response
        );
    }



    // 6. END PROCTORING SESSION

    @PostMapping(
            "/sessions/{sessionId}/end"
    )
    public ResponseEntity<ProctoringSessionResponse>
    endSession(
            @PathVariable Long sessionId
    ) {

        ProctoringSessionResponse response =
                proctoringSessionService.endSession(
                        sessionId
                );

        return ResponseEntity.ok(
                response
        );
    }


// GET SESSION SUMMARY

    @GetMapping(
            "/sessions/{sessionId}/summary"
    )
    public ResponseEntity<ProctoringSummaryResponse>
    getSessionSummary(
            @PathVariable Long sessionId
    ) {

        ProctoringSummaryResponse response =
                proctoringSessionService
                        .getSessionSummary(
                                sessionId
                        );

        return ResponseEntity.ok(
                response
        );
    }
}