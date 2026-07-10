package com.example.LMS_Ai_Proctoring.dto;

import lombok.Data;


@Data
public class MonitoringState {

    private String lastEvent;

    private String lastDirection;

    private int consecutiveCount;

    private int normalRecoveryCount;

    private boolean violationLocked;



    // RECORD SUSPICIOUS EVENT

    public void recordEvent(
            String event,
            String direction
    ) {

        // Suspicious frame aaya
        // normal recovery reset karo

        normalRecoveryCount = 0;


        /*
         * Same event + same direction
         *
         * Example:
         * LOOKING_AWAY + LOOKING_LEFT
         * LOOKING_AWAY + LOOKING_LEFT
         */

        if (
                event.equals(lastEvent)
                        &&
                        isSameDirection(
                                direction,
                                lastDirection
                        )
        ) {

            consecutiveCount++;

            return;
        }


        /*
         * Event ya direction change hui.
         *
         * Example:
         * LEFT -> RIGHT
         * RIGHT -> LEFT
         * LOOKING_AWAY -> NO_FACE
         *
         * Isko new suspicious sequence
         * treat karenge.
         */

        lastEvent =
                event;

        lastDirection =
                direction;

        consecutiveCount =
                1;


        // New sequence ke liye unlock

        violationLocked =
                false;
    }



    // RECORD NORMAL FRAME

    public void recordNormalFrame() {

        normalRecoveryCount++;
    }



    // LOCK VIOLATION

    public void lockViolation() {

        violationLocked =
                true;
    }



    // RESET AFTER NORMAL RECOVERY

    public void resetAfterRecovery() {

        lastEvent =
                null;

        lastDirection =
                null;

        consecutiveCount =
                0;

        normalRecoveryCount =
                0;

        violationLocked =
                false;
    }



    // CHECK SAME DIRECTION

    private boolean isSameDirection(
            String currentDirection,
            String previousDirection
    ) {

        if (currentDirection == null
                && previousDirection == null) {

            return true;
        }


        if (currentDirection == null
                || previousDirection == null) {

            return false;
        }


        return currentDirection.equals(
                previousDirection
        );
    }

    // RESET AFTER VIOLATION

    public void resetAfterViolation() {

        lastEvent =
                null;

        lastDirection =
                null;

        consecutiveCount =
                0;

        normalRecoveryCount =
                0;

        violationLocked =
                false;
    }
}