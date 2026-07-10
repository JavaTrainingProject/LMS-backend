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
        double noisePct = noiseDetectionService.calculateNoisePercentage(audioChunk);
        double speechPct = speechDetectionService.calculateSpeechPercentage(audioChunk);

        boolean noiseDetected = noiseDetectionService.isNoiseDetected(noisePct);
        boolean speechDetected = speechDetectionService.isSpeechDetected(speechPct);
        int speakerCount = speechDetectionService.estimateSpeakerCount(speechPct);

        return AudioAnalysisResult.builder()
                .noiseDetected(noiseDetected)
                .speechDetected(speechDetected)
                .noisePercentage(noisePct)
                .speechPercentage(speechPct)
                .speakerCount(speakerCount)
                .timestampMillis(System.currentTimeMillis())
                .build();
    }
}