package com.example.LMS_Ai_Proctoring.service;

import com.example.LMS_Ai_Proctoring.dto.SpeechToTextResult;
import com.example.LMS_Ai_Proctoring.entity.SpeechTranscript;
import com.example.LMS_Ai_Proctoring.repository.SpeechTranscriptRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vosk.Model;
import org.vosk.Recognizer;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SpeechToTextService {

    @Autowired
    private Model voskModel;

    @Autowired
    private SpeechTranscriptRepository speechTranscriptRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // One Recognizer per active session - keeps streaming context between chunks
    private final Map<Long, Recognizer> sessionRecognizers = new ConcurrentHashMap<>();

    private Recognizer getOrCreateRecognizer(Long sessionId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("sessionId cannot be null - was handshake sent?");
        }
        return sessionRecognizers.computeIfAbsent(sessionId, id -> {
            try {
                return new Recognizer(voskModel, 16000.0f);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create Vosk recognizer", e);
            }
        });
    }

    /**
     * Feed one audio chunk (from the WebSocket stream) into the recognizer
     * for this session. Returns partial or final transcribed text.
     */
    public SpeechToTextResult transcribeChunk(Long sessionId, byte[] audioChunk) {
        Recognizer recognizer = getOrCreateRecognizer(sessionId);

        try {
            boolean isFinal = recognizer.acceptWaveForm(audioChunk, audioChunk.length);
            String resultJson = isFinal ? recognizer.getResult() : recognizer.getPartialResult();

            JsonNode node = objectMapper.readTree(resultJson);
            String text = isFinal
                    ? node.path("text").asText("")
                    : node.path("partial").asText("");

            SpeechToTextResult result = SpeechToTextResult.builder()
                    .text(text)
                    .isFinal(isFinal)
                    .confidence(1.0) // Vosk doesn't expose per-word confidence in this simple mode
                    .build();

            // Persist only final, non-empty transcripts to MSSQL
            if (isFinal && !text.isBlank()) {
                saveTranscript(sessionId, text);
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Speech-to-text processing failed", e);
        }
    }

    private void saveTranscript(Long sessionId, String text) {
        SpeechTranscript transcript = SpeechTranscript.builder()
                .sessionId(sessionId)
                .transcribedText(text)
                .confidence(1.0)
                .detectedAt(LocalDateTime.now())
                .build();

        speechTranscriptRepository.save(transcript);
    }

    // Call when a proctoring session ends, to free native resources
    public void closeSession(Long sessionId) {
        Recognizer recognizer = sessionRecognizers.remove(sessionId);
        if (recognizer != null) {
            recognizer.close();
        }
    }
}
