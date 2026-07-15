package com.example.LMS_Ai_Proctoring.service;

import com.example.LMS_Ai_Proctoring.dto.SpeechToTextResult;
import com.example.LMS_Ai_Proctoring.entity.SpeechTranscript;
import com.example.LMS_Ai_Proctoring.repository.SpeechTranscriptRepository;
import com.example.LMS_Ai_Proctoring.responseDTO.SpeechToTextResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.vosk.Model;
import org.vosk.Recognizer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SpeechToTextService {

    private final Model voskModel;

    private final SpeechTranscriptRepository speechTranscriptRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * One streaming recognizer per session.
     */
    private final Map<Long, Recognizer> recognizers =
            new ConcurrentHashMap<>();

    private Recognizer getRecognizer(Long sessionId) {

        Recognizer recognizer = recognizers.get(sessionId);

        if (recognizer == null) {

            try {

                recognizer = new Recognizer(voskModel, 16000.0f);

                recognizers.put(sessionId, recognizer);

            } catch (Exception e) {

                throw new RuntimeException("Failed to create Vosk Recognizer", e);

            }

        }

        return recognizer;
    }

    /**
     * Accept raw PCM bytes from browser.
     */
    public SpeechToTextResult transcribeChunk(
            Long sessionId,
            byte[] pcmAudio
    ) {

        try {

            Recognizer recognizer =
                    getRecognizer(sessionId);

            boolean isFinal =
                    recognizer.acceptWaveForm(
                            pcmAudio,
                            pcmAudio.length
                    );

            String json =
                    isFinal
                            ? recognizer.getResult()
                            : recognizer.getPartialResult();

            JsonNode node =
                    objectMapper.readTree(json);

            String text =
                    isFinal
                            ? node.path("text").asText("")
                            : node.path("partial").asText("");

            if (isFinal && !text.isBlank()) {

                SpeechTranscript transcript =
                        SpeechTranscript.builder()
                                .sessionId(sessionId)
                                .transcribedText(text)
                                .confidence(1.0)
                                .detectedAt(LocalDateTime.now())
                                .build();

                speechTranscriptRepository.save(transcript);
            }

            return SpeechToTextResult.builder()
                    .text(text)
                    .isFinal(isFinal)
                    .confidence(1.0)
                    .build();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Speech recognition failed",
                    e
            );
        }

    }

    /**
     * Release Vosk native resources.
     */
    public void closeSession(Long sessionId) {

        Recognizer recognizer =
                recognizers.remove(sessionId);

        if (recognizer != null) {

            recognizer.close();

        }

    }

    /**
     * Fetch all stored transcripts.
     */
    public SpeechToTextResponse getTranscripts(Long sessionId) {

        List<String> transcripts =
                speechTranscriptRepository
                        .findBySessionIdOrderByDetectedAtAsc(sessionId)
                        .stream()
                        .map(SpeechTranscript::getTranscribedText)
                        .toList();

        return SpeechToTextResponse.builder()
                .sessionId(sessionId)
                .transcripts(transcripts)
                .message("Fetched Successfully")
                .build();
    }

}