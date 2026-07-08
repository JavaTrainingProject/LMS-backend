package com.example.LMS_Ai_Proctoring.service;

import com.example.LMS_Ai_Proctoring.dto.AudioAnalysisResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AudioAnalysisService {

    @Autowired
    private NoiseDetectionService noiseDetectionService;

    @Autowired
    private SpeechDetectionService speechDetectionService;

    public AudioAnalysisResult analyze(byte[] audioChunk) {
        double decibelLevel = noiseDetectionService.calculateDecibelLevel(audioChunk);
        double energyLevel = speechDetectionService.calculateEnergyLevel(audioChunk);

        boolean noiseDetected = noiseDetectionService.isNoiseDetected(decibelLevel);
        boolean speechDetected = speechDetectionService.isSpeechDetected(energyLevel);
        int speakerCount = speechDetectionService.estimateSpeakerCount(energyLevel);

        return AudioAnalysisResult.builder()
                .noiseDetected(noiseDetected)
                .speechDetected(speechDetected)
                .decibelLevel(decibelLevel)
                .energyLevel(energyLevel)
                .speakerCount(speakerCount)
                .timestampMillis(System.currentTimeMillis())
                .build();
    }
}
