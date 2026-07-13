package com.example.LMS_Ai_Proctoring.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoiseDetectionResponse {
    private boolean noiseDetected;
    private double decibelLevel;
    private String severity;   // "LOW", "MODERATE", "HIGH"
    private String message;
}
