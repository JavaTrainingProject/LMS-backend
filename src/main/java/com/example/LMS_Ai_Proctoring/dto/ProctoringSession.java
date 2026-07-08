package com.example.LMS_Ai_Proctoring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ProctoringSession {

    private Long sessionId;

    private String status;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;
}