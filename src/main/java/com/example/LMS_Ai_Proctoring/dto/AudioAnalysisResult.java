package com.example.LMS_Ai_Proctoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AudioAnalysisResult {


    private boolean noiseDetected;

    private boolean speechDetected;

    private int speakerCount;

    private double noisePercentage;


    private double speechPercentage;

    private long timestampMillis;
}