package com.example.LMS_Ai_Proctoring.exception;

public class AudioProcessingException extends RuntimeException {
    public AudioProcessingException(String message) {
        super(message);
    }

    public AudioProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
