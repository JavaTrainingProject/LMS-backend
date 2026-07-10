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
    private double noisePercentage;   // 0-100
    private double speechPercentage;  // 0-100 (same underlying value as noisePercentage now)
    private int speakerCount;
    private long timestampMillis;
}