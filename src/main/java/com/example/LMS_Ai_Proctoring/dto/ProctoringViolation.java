package com.example.LMS_Ai_Proctoring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
public class ProctoringViolation {

    private Long violationId;

    private Long sessionId;

    private String eventType;

    private int faceCount;

    private String headDirection;

    private int consecutiveCount;

    private LocalDateTime detectedAt;
}