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
    private double decibelLevel;
    private double energyLevel;
    private int speakerCount;      // if you extend to multi-speaker detection later
    private long timestampMillis;
}
