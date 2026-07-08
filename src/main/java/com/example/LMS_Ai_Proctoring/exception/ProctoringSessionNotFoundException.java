package com.example.LMS_Ai_Proctoring.exception;

public class ProctoringSessionNotFoundException
        extends RuntimeException {

    public ProctoringSessionNotFoundException(
            String message
    ) {
        super(message);
    }
}