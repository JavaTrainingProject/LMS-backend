package com.example.LMS_Ai_Proctoring.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProctoringSessionResponse {

    private Long sessionId;

    private String status;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private String message;
}