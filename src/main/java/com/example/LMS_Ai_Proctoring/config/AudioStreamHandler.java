package com.example.LMS_Ai_Proctoring.config;

import com.example.LMS_Ai_Proctoring.dto.AudioAnalysisResult;
import com.example.LMS_Ai_Proctoring.service.AudioAnalysisService;
import com.example.LMS_Ai_Proctoring.service.ProctoringViolationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

@Component
public class AudioStreamHandler extends AbstractWebSocketHandler {

    @Autowired
    private AudioAnalysisService audioAnalysisService;

    @Autowired
    private ProctoringViolationService proctoringViolationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        byte[] audioChunk = message.getPayload().array();

        // Session must carry the proctoring sessionId, e.g. set at handshake
        // or sent as the first text frame — see note below.
        // in AudioStreamHandler.handleBinaryMessage()
        Long sessionId = (Long) session.getAttributes().get("sessionId");

        AudioAnalysisResult result = audioAnalysisService.analyze(audioChunk);

        // Fire violation checks (noise/speech thresholds)
        proctoringViolationService.evaluateAudioResult(sessionId, result);

        // Send live feedback back to the client
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(result)));
    }

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
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        session.getAttributes().remove("sessionId");
    }
}
