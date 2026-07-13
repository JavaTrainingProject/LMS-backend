package com.example.LMS_Ai_Proctoring.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeechDetectionResponse {
    private boolean speechDetected;
    private int speakerCount;
    private double energyLevel;
    private String message;
}
