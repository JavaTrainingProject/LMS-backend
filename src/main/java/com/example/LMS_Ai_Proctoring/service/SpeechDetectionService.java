package com.example.LMS_Ai_Proctoring.service;

import com.example.LMS_Ai_Proctoring.config.AudioConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SpeechDetectionService {

    @Autowired
    private AudioLevelCalculator audioLevelCalculator;

    public double calculateSpeechPercentage(byte[] audioData) {
        return audioLevelCalculator.calculateNormalizedRms(audioData) * 100;
    }

    public boolean isSpeechDetected(double speechPercentage) {
        return speechPercentage >= AudioConfig.SPEECH_ENERGY_THRESHOLD * 100;
    }

    public int estimateSpeakerCount(double speechPercentage) {
        return isSpeechDetected(speechPercentage) ? 1 : 0;
    }
}