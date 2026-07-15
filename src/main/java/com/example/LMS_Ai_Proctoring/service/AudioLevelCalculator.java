package com.example.LMS_Ai_Proctoring.service;

import org.springframework.stereotype.Component;

@Component
public class AudioLevelCalculator {

    public double calculateNormalizedRms(byte[] pcmData) {

        if (pcmData == null || pcmData.length < 2)
            return 0;

        long sum = 0;

        int samples = pcmData.length / 2;

        for (int i = 0; i < pcmData.length - 1; i += 2) {

            short sample = (short) (
                    (pcmData[i] & 0xff)
                            | (pcmData[i + 1] << 8)
            );

            sum += sample * sample;
        }

        double rms =
                Math.sqrt(sum / (double) samples);

        return rms / 32768.0;
    }
}