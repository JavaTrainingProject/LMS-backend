package com.example.LMS_Ai_Proctoring.service;

import com.example.LMS_Ai_Proctoring.config.AudioConfig;
import com.example.LMS_Ai_Proctoring.exception.AudioProcessingException;
import org.springframework.stereotype.Service;

@Service
public class SpeechDetectionService {

    /**
     * Simple energy-based Voice Activity Detection (VAD).
     * For production-grade accuracy, replace this with TarsosDSP's
     * SilenceDetector or a WebRTC VAD binding.
     */
    public double calculateEnergyLevel(byte[] audioData) {
        try {
            double sum = 0;
            int sampleCount = audioData.length / 2;
            for (int i = 0; i < audioData.length - 1; i += 2) {
                short sample = (short) ((audioData[i + 1] << 8) | (audioData[i] & 0xFF));
                double normalized = sample / 32768.0;
                sum += normalized * normalized;
            }
            return Math.sqrt(sum / sampleCount);
        } catch (Exception e) {
            throw new AudioProcessingException("Failed to calculate energy level", e);
        }
    }

    public boolean isSpeechDetected(double energyLevel) {
        return energyLevel >= AudioConfig.SPEECH_ENERGY_THRESHOLD;
    }

    // Placeholder — real multi-speaker detection needs diarization
    // (e.g. pyannote via a Python microservice, or a speaker-embedding model).
    public int estimateSpeakerCount(double energyLevel) {
        return isSpeechDetected(energyLevel) ? 1 : 0;
    }
}
