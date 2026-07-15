package com.example.LMS_Ai_Proctoring.service;

import com.example.LMS_Ai_Proctoring.config.AudioConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpeechDetectionService {

    private final AudioLevelCalculator audioLevelCalculator;

    /**
     * Calculates speech energy from raw PCM audio.
     */
    public double calculateSpeechPercentage(byte[] pcmAudio) {

        double rms =
                audioLevelCalculator.calculateNormalizedRms(pcmAudio);

        return Math.min(rms * 100.0, 100.0);
    }

    /**
     * Returns whether speech is present.
     */
    public boolean isSpeechDetected(double speechPercentage) {

        return speechPercentage >= AudioConfig.SPEECH_THRESHOLD_PERCENT;
    }

    /**
     * Placeholder until speaker diarization is added.
     */
    public int estimateSpeakerCount(byte[] pcmAudio) {

        double speech =
                calculateSpeechPercentage(pcmAudio);

        return speech >= AudioConfig.SPEECH_THRESHOLD_PERCENT ? 1 : 0;
    }
}