package com.example.LMS_Ai_Proctoring.service;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AudioConversionService {

    private static final Logger logger = LoggerFactory.getLogger(AudioConversionService.class);

    private static final int TARGET_SAMPLE_RATE = 16000;
    private static final int TARGET_CHANNELS = 1; // mono
    private static final String FFMPEG_PATH = "ffmpeg"; // ensure ffmpeg is on system PATH

    /**
     * Converts raw WebM (opus) audio bytes into 16kHz mono PCM WAV bytes.
     *
     * @param webmAudio raw bytes as received from the browser (audio/webm)
     * @return PCM WAV byte array (16kHz, mono, 16-bit)
     */
    public byte[] convert(byte[] webmAudio) {
        Path tempDir = null;
        Path inputFile = null;
        Path outputFile = null;

        try {
            tempDir = Files.createTempDirectory("audio_conv_");
            String uid = UUID.randomUUID().toString();
            inputFile = tempDir.resolve(uid + "_input.webm");
            outputFile = tempDir.resolve(uid + "_output.wav");

            // Write incoming bytes to a temp file
            Files.write(inputFile, webmAudio);

            // Build ffmpeg command:
            // -i input.webm -ar 16000 -ac 1 -f wav -acodec pcm_s16le output.wav
            ProcessBuilder pb = new ProcessBuilder(
                    FFMPEG_PATH,
                    "-y",                      // overwrite output if exists
                    "-i", inputFile.toString(),
                    "-ar", String.valueOf(TARGET_SAMPLE_RATE),
                    "-ac", String.valueOf(TARGET_CHANNELS),
                    "-f", "wav",
                    "-acodec", "pcm_s16le",
                    outputFile.toString()
            );
            pb.redirectErrorStream(true);

            Process process = pb.start();

            // Drain output to avoid process hanging on full buffer
            StringBuilder ffmpegLog = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    ffmpegLog.append(line).append("\n");
                }
            }

            boolean finished = process.waitFor(15, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new AudioConversionException("FFmpeg conversion timed out");
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                logger.error("FFmpeg conversion failed. Exit code: {}. Log:\n{}", exitCode, ffmpegLog);
                throw new AudioConversionException("FFmpeg exited with code " + exitCode);
            }

            if (!Files.exists(outputFile) || Files.size(outputFile) == 0) {
                throw new AudioConversionException("FFmpeg produced no output audio");
            }

            return Files.readAllBytes(outputFile);

        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            logger.error("Audio conversion failed", e);
            throw new AudioConversionException("Failed to convert audio: " + e.getMessage(), e);
        } finally {
            // Clean up temp files
            deleteQuietly(inputFile);
            deleteQuietly(outputFile);
            deleteQuietly(tempDir);
        }
    }

    private void deleteQuietly(Path path) {
        if (path == null) return;
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            logger.warn("Failed to delete temp file: {}", path, e);
        }
    }

    public static class AudioConversionException extends RuntimeException {
        public AudioConversionException(String message) {
            super(message);
        }
        public AudioConversionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}