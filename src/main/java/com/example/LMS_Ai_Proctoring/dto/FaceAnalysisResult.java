package com.example.LMS_Ai_Proctoring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FaceAnalysisResult {

    private int faceCount;

    private String headDirection;
}