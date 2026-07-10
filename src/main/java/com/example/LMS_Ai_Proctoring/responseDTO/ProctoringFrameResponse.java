package com.example.LMS_Ai_Proctoring.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProctoringFrameResponse {

    private Long sessionId;

    private boolean violationConfirmed;

    private String eventType;

    private int consecutiveCount;

    private int faceCount;

    private String headDirection;

    private String message;
}