package com.example.LMS_Ai_Proctoring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "speech_transcripts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeechTranscript {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sessionId;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String transcribedText;

    private double confidence;

    private LocalDateTime detectedAt;
}