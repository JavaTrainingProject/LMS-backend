package com.example.LMS_Ai_Proctoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProctoringViolation {

    private Long violationId;

    private Long sessionId;

    private String eventType;

    private int faceCount;

    private String headDirection;

    private String gazeDirection;

    // ---- new audio fields ----
    private Double decibelLevel;

    private Boolean speechDetected;

    private Integer speakerCount;
    // ---------------------------

    private LocalDateTime detectedAt;
}