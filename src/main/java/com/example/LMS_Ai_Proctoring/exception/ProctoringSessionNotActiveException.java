package com.example.LMS_Ai_Proctoring.exception;

public class ProctoringSessionNotActiveException
        extends RuntimeException {

    public ProctoringSessionNotActiveException(
            String message
    ) {
        super(message);
    }
}