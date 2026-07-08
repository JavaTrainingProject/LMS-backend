package com.example.LMS_Ai_Proctoring.service;

import com.example.LMS_Ai_Proctoring.config.AudioConfig;
import com.example.LMS_Ai_Proctoring.exception.AudioProcessingException;
import org.springframework.stereotype.Service;

@Service
public class NoiseDetectionService {

    /**
     * Computes decibel level from raw PCM audio bytes and flags loud noise.
     */
    public double calculateDecibelLevel(byte[] audioData) {
        try {
            long sum = 0;
            for (int i = 0; i < audioData.length - 1; i += 2) {
                // little-endian 16-bit sample
                short sample = (short) ((audioData[i + 1] << 8) | (audioData[i] & 0xFF));
                sum += (long) sample * sample;
            }
            double rms = Math.sqrt(sum / (double) (audioData.length / 2));
            if (rms <= 0) return 0.0;

            // Convert RMS to approximate decibels
            return 20 * Math.log10(rms);
        } catch (Exception e) {
            throw new AudioProcessingException("Failed to calculate decibel level", e);
        }
    }

    public boolean isNoiseDetected(double decibelLevel) {
        return decibelLevel >= AudioConfig.NOISE_DB_THRESHOLD;
    }

    public String classifySeverity(double decibelLevel) {
        if (decibelLevel < AudioConfig.NOISE_DB_THRESHOLD) return "LOW";
        if (decibelLevel < AudioConfig.NOISE_DB_THRESHOLD + 15) return "MODERATE";
        return "HIGH";
    }
}
