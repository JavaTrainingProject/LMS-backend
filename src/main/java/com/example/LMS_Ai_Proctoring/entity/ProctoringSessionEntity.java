package com.example.LMS_Ai_Proctoring.entity;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "proctoring_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProctoringSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private String status;


    @Column(
            name = "started_at",
            nullable = false
    )
    private LocalDateTime startedAt;


    @Column(
            name = "ended_at"
    )
    private LocalDateTime endedAt;



    // TOTAL VIOLATIONS

    @Column(
            name = "total_violations",
            nullable = false
    )
    @Builder.Default
    private Integer totalViolations = 0;



    // LOOKING LEFT COUNT

    @Column(
            name = "looking_left_count",
            nullable = false
    )
    @Builder.Default
    private Integer lookingLeftCount = 0;



    // LOOKING RIGHT COUNT

    @Column(
            name = "looking_right_count",
            nullable = false
    )
    @Builder.Default
    private Integer lookingRightCount = 0;



    // LOOKING UP COUNT

    @Column(
            name = "looking_up_count",
            nullable = false
    )
    @Builder.Default
    private Integer lookingUpCount = 0;



    // LOOKING DOWN COUNT

    @Column(
            name = "looking_down_count",
            nullable = false
    )
    @Builder.Default
    private Integer lookingDownCount = 0;



    // NO FACE COUNT

    @Column(
            name = "no_face_count",
            nullable = false
    )
    @Builder.Default
    private Integer noFaceCount = 0;



    // MULTIPLE FACES COUNT

    @Column(
            name = "multiple_faces_count",
            nullable = false
    )
    @Builder.Default
    private Integer multipleFacesCount = 0;
}