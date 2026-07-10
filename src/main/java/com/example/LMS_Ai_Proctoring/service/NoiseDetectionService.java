package com.example.LMS_Ai_Proctoring.service;

import com.example.LMS_Ai_Proctoring.config.AudioConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NoiseDetectionService {

    @Autowired
    private AudioLevelCalculator audioLevelCalculator;

    public double calculateNoisePercentage(byte[] audioData) {
        return audioLevelCalculator.calculateNormalizedRms(audioData) * 100;
    }

    public boolean isNoiseDetected(double noisePercentage) {
        return noisePercentage >= AudioConfig.NOISE_DB_THRESHOLD * 100;
    }

    public String classifySeverity(double noisePercentage) {
        double thresholdPct = AudioConfig.NOISE_DB_THRESHOLD * 100;
        if (noisePercentage < thresholdPct) return "LOW";
        if (noisePercentage < thresholdPct + 20) return "MODERATE";
        return "HIGH";
    }
}