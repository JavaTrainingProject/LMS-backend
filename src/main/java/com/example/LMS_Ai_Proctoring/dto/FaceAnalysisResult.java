package com.example.LMS_Ai_Proctoring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FaceAnalysisResult {

    private int faceCount;

    private String headDirection;

    private String gazeDirection;

    private boolean eyesDetected;

    private double rightEyeX;
    private double rightEyeY;

    private double leftEyeX;
    private double leftEyeY;
}