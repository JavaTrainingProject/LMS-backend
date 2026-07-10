package com.example.LMS_Ai_Proctoring.entity;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "proctoring_violations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProctoringViolationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(
            name = "session_id",
            nullable = false
    )
    private Long sessionId;


    @Column(
            name = "event_type",
            nullable = false,
            length = 50
    )
    private String eventType;


    @Column(
            name = "face_count",
            nullable = false
    )
    private int faceCount;


    /*
     * LOOKING_AWAY ke liye:
     * LOOKING_LEFT / LOOKING_RIGHT
     *
     * NO_FACE_DETECTED
     * MULTIPLE_FACES_DETECTED  null.
     */
    @Column(
            name = "head_direction",
            length = 30
    )
    private String headDirection;


    @Column(
            name = "consecutive_count",
            nullable = false
    )
    private int consecutiveCount;


    @Column(
            name = "detected_at",
            nullable = false
    )
    private LocalDateTime detectedAt;
}