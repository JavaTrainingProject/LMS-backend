package com.example.LMS_Ai_Proctoring.exception;

import com.example.LMS_Ai_Proctoring.responseDTO.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {


    // ==========================================
    // SESSION NOT ACTIVE
    // ==========================================

    @ExceptionHandler(
            ProctoringSessionNotActiveException.class
    )
    public ResponseEntity<ErrorResponse>
    handleSessionNotActive(
            ProctoringSessionNotActiveException ex,
            HttpServletRequest request
    ) {

        ErrorResponse response =
                ErrorResponse.builder()
                        .status(
                                HttpStatus.CONFLICT.value()
                        )
                        .success(false)
                        .message(
                                ex.getMessage()
                        )
                        .timestamp(
                                LocalDateTime.now()
                        )
                        .path(
                                request.getRequestURI()
                        )
                        .build();

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }


    // ==========================================
    // SESSION NOT FOUND
    // ==========================================

    @ExceptionHandler(
            ProctoringSessionNotFoundException.class
    )
    public ResponseEntity<ErrorResponse>
    handleSessionNotFound(
            ProctoringSessionNotFoundException ex,
            HttpServletRequest request
    ) {

        ErrorResponse response =
                ErrorResponse.builder()
                        .status(
                                HttpStatus.NOT_FOUND.value()
                        )
                        .success(false)
                        .message(
                                ex.getMessage()
                        )
                        .timestamp(
                                LocalDateTime.now()
                        )
                        .path(
                                request.getRequestURI()
                        )
                        .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }
}