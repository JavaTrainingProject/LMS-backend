package com.example.LMS_Ai_Proctoring.responseDTO;

import com.example.LMS_Ai_Proctoring.dto.ProctoringViolation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProctoringViolationResponse {

    private Long sessionId;

    private int totalViolations;

    private List<ProctoringViolation> violations;

    private String message;
}