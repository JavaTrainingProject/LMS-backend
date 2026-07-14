package com.example.LMS_Ai_Proctoring.responseDTO;

import com.example.LMS_Ai_Proctoring.dto.AudioAnalysisResult;
import com.example.LMS_Ai_Proctoring.dto.AudioWarningResult;
import com.example.LMS_Ai_Proctoring.dto.SpeechToTextResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AudioMonitoringResponse {

    private AudioAnalysisResult audio;

    private SpeechToTextResult transcript;

    private AudioWarningResult warning;

}
