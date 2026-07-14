package com.example.LMS_Ai_Proctoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AudioWarningResult {

    private boolean warningGenerated;

    private int warningCount;

    private boolean terminateAssessment;

    private String message;

}
