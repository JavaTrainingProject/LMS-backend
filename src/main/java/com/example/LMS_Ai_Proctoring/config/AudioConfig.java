package com.example.LMS_Ai_Proctoring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sound.sampled.AudioFormat;

@Configuration
public class AudioConfig {

    // 16kHz, 16-bit, mono - standard for speech/noise analysis
    @Bean
    public AudioFormat audioFormat() {
        float sampleRate = 16000.0f;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }

    // Threshold in dB above which audio is flagged as "loud noise"
    public static final double NOISE_DB_THRESHOLD = 0.15;

    // Minimum RMS energy for a frame to be considered "speech-like"
    public static final double SPEECH_ENERGY_THRESHOLD = 0.04;
}
