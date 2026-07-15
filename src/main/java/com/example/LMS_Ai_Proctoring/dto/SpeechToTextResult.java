package com.example.LMS_Ai_Proctoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeechToTextResult {
    private String text;
    private boolean isFinal;   // true = final result, false = partial (still listening)
    private double confidence;
}
