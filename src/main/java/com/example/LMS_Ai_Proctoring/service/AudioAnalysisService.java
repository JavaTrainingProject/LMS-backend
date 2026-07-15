package com.example.LMS_Ai_Proctoring.service;

import com.example.LMS_Ai_Proctoring.dto.AudioAnalysisResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AudioAnalysisService {

    private final NoiseDetectionService noiseDetectionService;

    private final SpeechDetectionService speechDetectionService;

    public AudioAnalysisResult analyze(byte[] pcmAudio) {

        double noisePercentage =
                noiseDetectionService.calculateNoisePercentage(pcmAudio);

        double speechPercentage =
                speechDetectionService.calculateSpeechPercentage(pcmAudio);

        boolean noiseDetected =
                noiseDetectionService.isNoiseDetected(noisePercentage);

        boolean speechDetected =
                speechDetectionService.isSpeechDetected(speechPercentage);

        int speakerCount =
                speechDetectionService.estimateSpeakerCount(pcmAudio);

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