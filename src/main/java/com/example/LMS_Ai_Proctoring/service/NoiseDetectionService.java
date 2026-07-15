package com.example.LMS_Ai_Proctoring.service;

import com.example.LMS_Ai_Proctoring.config.AudioConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoiseDetectionService {

    private final AudioLevelCalculator audioLevelCalculator;

    /**
     * Calculate microphone volume percentage.
     */
    public double calculateNoisePercentage(byte[] pcmAudio) {

        double rms =
                audioLevelCalculator.calculateNormalizedRms(pcmAudio);

        return Math.min(rms * 100.0, 100.0);
    }

    /**
     * Whether microphone volume exceeds configured threshold.
     */
    public boolean isNoiseDetected(double noisePercentage) {

        return noisePercentage >= AudioConfig.NOISE_THRESHOLD_PERCENT;
    }

    /**
     * Optional severity used by UI.
     */
    public String classifySeverity(double noisePercentage) {

        if (noisePercentage < 25)
            return "LOW";

        if (noisePercentage < 60)
            return "MEDIUM";

        return "HIGH";
    }
}