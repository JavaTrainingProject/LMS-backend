package com.example.LMS_Ai_Proctoring.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AudioWarningResponse {

    private boolean noiseDetected;

    private boolean speechDetected;

    private int speakerCount;

    private boolean warningGenerated;

    private String warningMessage;

    private int warningCount;

    private boolean terminateAssessment;

    private double noisePercentage;

    private double speechPercentage;
}