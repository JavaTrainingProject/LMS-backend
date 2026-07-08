package com.example.LMS_Ai_Proctoring.responseDTO;

import com.example.LMS_Ai_Proctoring.enums.ProctoringEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaceDetectionResponse {

    private int faceCount;

    private ProctoringEventType eventType;

    private boolean violation;

    private String message;
}