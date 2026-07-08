package com.example.LMS_Ai_Proctoring.dto;

import lombok.Getter;

@Getter
public class MonitoringState {

    private String lastEvent;

    private int consecutiveCount;

    /*
     * Current continuous suspicious activity
     * already save hui hai ya nahi.
     */
    private boolean violationLocked;

    /*
     * Consecutive NORMAL frames count.
     */
    private int normalRecoveryCount;



    // RECORD SUSPICIOUS EVENT

    public synchronized void recordEvent(
            String currentEvent
    ) {

        if (currentEvent == null) {
            return;
        }

        /*
         * Suspicious frame aaya,
         * so normal recovery sequence break.
         */
        normalRecoveryCount = 0;


        // Same suspicious event continues
        if (currentEvent.equals(lastEvent)) {

            consecutiveCount++;

            return;
        }


        /*
         * Different suspicious event starts.
         *
         * Example:
         * LOOKING_AWAY
         *      ↓
         * NO_FACE_DETECTED
         */
        lastEvent = currentEvent;

        consecutiveCount = 1;

        violationLocked = false;
    }



    // RECORD NORMAL FRAME

    public synchronized void recordNormalFrame() {

        normalRecoveryCount++;
    }



    // LOCK CONFIRMED VIOLATION

    public synchronized void lockViolation() {

        violationLocked = true;
    }



    // RESET AFTER CONFIRMED RECOVERY

    public synchronized void resetAfterRecovery() {

        lastEvent = null;

        consecutiveCount = 0;

        violationLocked = false;

        normalRecoveryCount = 0;
    }



    // CLEAR COMPLETE STATE

    public synchronized void reset() {

        lastEvent = null;

        consecutiveCount = 0;

        violationLocked = false;

        normalRecoveryCount = 0;
    }
}