package com.example.LMS_Ai_Proctoring.config;

import com.example.LMS_Ai_Proctoring.dto.AudioAnalysisResult;
import com.example.LMS_Ai_Proctoring.dto.SpeechToTextResult;
import com.example.LMS_Ai_Proctoring.service.AudioAnalysisService;
import com.example.LMS_Ai_Proctoring.service.ProctoringViolationService;
import com.example.LMS_Ai_Proctoring.service.SpeechToTextService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.util.HashMap;
import java.util.Map;

@Component
public class AudioStreamHandler extends AbstractWebSocketHandler {

    @Autowired
    private AudioAnalysisService audioAnalysisService;

    @Autowired
    private ProctoringViolationService proctoringViolationService;

    @Autowired
    private SpeechToTextService speechToTextService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Expect first message from client to be: {"sessionId": "..."}
        String payload = message.getPayload();
        if (payload.contains("sessionId")) {
            var node = objectMapper.readTree(payload);
            session.getAttributes().put("sessionId", node.get("sessionId").asLong());
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        byte[] audioChunk = message.getPayload().array();
        Long sessionId = (Long) session.getAttributes().get("sessionId");

        System.out.println("### DEBUG: sessionId = " + sessionId);
        if (sessionId == null) {
            session.sendMessage(new TextMessage("{\"error\":\"sessionId not set - send handshake first\"}"));
            return;
        }

        AudioAnalysisResult noiseResult = audioAnalysisService.analyze(audioChunk);
        proctoringViolationService.evaluateAudioResult(sessionId, noiseResult);

        SpeechToTextResult sttResult = speechToTextService.transcribeChunk(sessionId, audioChunk);

        Map<String, Object> combinedResponse = new HashMap<>();
        combinedResponse.put("audio", noiseResult);
        combinedResponse.put("transcript", sttResult);

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(combinedResponse)));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long sessionId = (Long) session.getAttributes().get("sessionId");
        if (sessionId != null) {
            speechToTextService.closeSession(sessionId);
        }
        session.getAttributes().remove("sessionId");
    }
}
