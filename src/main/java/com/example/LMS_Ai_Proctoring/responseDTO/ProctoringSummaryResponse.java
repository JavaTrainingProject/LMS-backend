package com.example.LMS_Ai_Proctoring.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProctoringSummaryResponse {

    private Long sessionId;

    private String status;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private int totalViolations;

    private Map<String, Long> violationCounts;

    private String message;
}