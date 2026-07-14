package com.example.LMS_Ai_Proctoring.service;

import com.example.LMS_Ai_Proctoring.dto.AudioAnalysisResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AudioAnalysisService {


    @Autowired
    private final NoiseDetectionService noiseDetectionService;

    @Autowired
    private final SpeechDetectionService speechDetectionService;

    public AudioAnalysisResult analyze(byte[] audioChunk) {

        double noisePercentage =
                noiseDetectionService.calculateNoisePercentage(audioChunk);

        double speechPercentage =
                speechDetectionService.calculateSpeechPercentage(audioChunk);

        boolean noiseDetected =
                noiseDetectionService.isNoiseDetected(noisePercentage);

        boolean speechDetected =
                speechDetectionService.isSpeechDetected(speechPercentage);

        int speakerCount =
                speechDetectionService.estimateSpeakerCount(speechPercentage);

        return AudioAnalysisResult.builder()
                .noiseDetected(noiseDetected)
                .speechDetected(speechDetected)
                .speakerCount(speakerCount)
                .noisePercentage(noisePercentage)
                .speechPercentage(speechPercentage)
                .timestampMillis(System.currentTimeMillis())
                .build();
    }
}