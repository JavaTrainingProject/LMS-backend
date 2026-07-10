package com.example.LMS_Ai_Proctoring.service;

import org.springframework.stereotype.Component;

@Component
public class AudioLevelCalculator {

    /**
     * Returns normalized RMS amplitude as a 0.0–1.0 value
     * (multiply by 100 to get a percentage).
     */
    public double calculateNormalizedRms(byte[] audioData) {
        if (audioData == null || audioData.length < 2) return 0.0;

        long sumSquares = 0;
        int sampleCount = audioData.length / 2;

        for (int i = 0; i < audioData.length - 1; i += 2) {
            short sample = (short) ((audioData[i + 1] << 8) | (audioData[i] & 0xFF));
            sumSquares += (long) sample * sample;
        }

        double rms = Math.sqrt(sumSquares / (double) sampleCount);
        return rms / 32768.0; // normalize to 0.0–1.0
    }
}
