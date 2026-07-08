package com.example.LMS_Ai_Proctoring.service;

import com.example.LMS_Ai_Proctoring.dto.FaceAnalysisResult;
import jakarta.annotation.PostConstruct;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;

import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.FaceDetectorYN;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Service
public class FaceDetectionService {

    private FaceDetectorYN faceDetector;

    private static final double HEAD_THRESHOLD = 0.15;

    private static final double GAZE_LEFT_THRESHOLD = 0.38;
    private static final double GAZE_RIGHT_THRESHOLD = 0.62;


    // INITIALIZE YUNET MODEL

    @PostConstruct
    public void initializeDetector() throws IOException {

        ClassPathResource resource =
                new ClassPathResource(
                        "models/face_detection_yunet_2023mar.onnx"
                );

        File tempModelFile =
                File.createTempFile(
                        "face_detection_yunet_",
                        ".onnx"
                );

        tempModelFile.deleteOnExit();

        try (var inputStream = resource.getInputStream()) {

            Files.copy(
                    inputStream,
                    tempModelFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );
        }

        faceDetector = FaceDetectorYN.create(
                tempModelFile.getAbsolutePath(),
                "",
                new Size(320, 320),
                0.9f,
                0.3f,
                5000
        );

        System.out.println(
                "YuNet model loaded successfully"
        );
    }

    // MAIN FACE ANALYSIS
    public FaceAnalysisResult analyzeFace(
            MultipartFile file
    ) throws IOException {

        if (file == null || file.isEmpty()) {

            throw new IllegalArgumentException(
                    "Image file is required"
            );
        }


        MatOfByte imageBuffer =
                new MatOfByte(
                        file.getBytes()
                );


        Mat image =
                Imgcodecs.imdecode(
                        imageBuffer,
                        Imgcodecs.IMREAD_COLOR
                );

        imageBuffer.release();


        if (image.empty()) {

            image.release();

            throw new IllegalArgumentException(
                    "Invalid image file"
            );
        }


        Mat faces =
                new Mat();


        try {

            faceDetector.setInputSize(
                    new Size(
                            image.width(),
                            image.height()
                    )
            );


            faceDetector.detect(
                    image,
                    faces
            );


            int faceCount =
                    faces.rows();



            // NO FACE
            if (faceCount == 0) {

                return new FaceAnalysisResult(
                        0,
                        "NO_FACE",
                        "UNKNOWN",
                        false,
                        0,
                        0,
                        0,
                        0
                );
            }


               // MULTIPLE FACES
               if (faceCount > 1) {

                return new FaceAnalysisResult(
                        faceCount,
                        "MULTIPLE_FACES",
                        "UNKNOWN",
                        false,
                        0,
                        0,
                        0,
                        0
                );
            }


            // YUNET EYE LANDMARKS
                      double rightEyeX =
                    faces.get(0, 4)[0];

            double rightEyeY =
                    faces.get(0, 5)[0];


            double leftEyeX =
                    faces.get(0, 6)[0];

            double leftEyeY =
                    faces.get(0, 7)[0];


            double noseX =
                    faces.get(0, 8)[0];


            // CHECK EYE LANDMARKS

            boolean eyesDetected =
                    areEyesDetected(
                            rightEyeX,
                            rightEyeY,
                            leftEyeX,
                            leftEyeY,
                            image.width(),
                            image.height()
                    );



            // HEAD DIRECTION
            String headDirection =
                    calculateHeadDirection(
                            rightEyeX,
                            leftEyeX,
                            noseX
                    );


            // GAZE DIRECTION

            String gazeDirection;


            if (eyesDetected) {

                gazeDirection =
                        calculateGazeDirection(
                                image,
                                rightEyeX,
                                rightEyeY,
                                leftEyeX,
                                leftEyeY
                        );

            } else {

                gazeDirection =
                        "UNKNOWN";
            }


            // DEBUG LOGS
            System.out.println(
                    "================================="
            );

            System.out.println(
                    "Face Count: "
                            + faceCount
            );

            System.out.println(
                    "Eyes Detected: "
                            + eyesDetected
            );

            System.out.println(
                    "Right Eye: ("
                            + rightEyeX
                            + ", "
                            + rightEyeY
                            + ")"
            );

            System.out.println(
                    "Left Eye: ("
                            + leftEyeX
                            + ", "
                            + leftEyeY
                            + ")"
            );

            System.out.println(
                    "Head Direction: "
                            + headDirection
            );

            System.out.println(
                    "Gaze Direction: "
                            + gazeDirection
            );

            System.out.println(
                    "================================="
            );


            // FINAL RESULT
            return new FaceAnalysisResult(
                    faceCount,
                    headDirection,
                    gazeDirection,
                    eyesDetected,
                    rightEyeX,
                    rightEyeY,
                    leftEyeX,
                    leftEyeY
            );

        } finally {

            faces.release();
            image.release();
        }
    }


    // CHECK WHETHER EYE LANDMARKS ARE VALID
    private boolean areEyesDetected(
            double rightEyeX,
            double rightEyeY,
            double leftEyeX,
            double leftEyeY,
            int imageWidth,
            int imageHeight
    ) {

        boolean rightEyeValid =
                rightEyeX >= 0
                        && rightEyeX < imageWidth
                        && rightEyeY >= 0
                        && rightEyeY < imageHeight;


        boolean leftEyeValid =
                leftEyeX >= 0
                        && leftEyeX < imageWidth
                        && leftEyeY >= 0
                        && leftEyeY < imageHeight;


        double eyeDistance =
                Math.abs(
                        leftEyeX - rightEyeX
                );


        return rightEyeValid
                && leftEyeValid
                && eyeDistance > 5;
    }


    // HEAD DIRECTION

    private String calculateHeadDirection(
            double rightEyeX,
            double leftEyeX,
            double noseX
    ) {

        double minEyeX =
                Math.min(
                        rightEyeX,
                        leftEyeX
                );


        double maxEyeX =
                Math.max(
                        rightEyeX,
                        leftEyeX
                );


        double eyeDistance =
                maxEyeX - minEyeX;


        if (eyeDistance <= 0) {

            return "FRONT";
        }


        double normalizedNosePosition =
                (noseX - minEyeX)
                        / eyeDistance;


        System.out.println(
                "Normalized Nose Position: "
                        + normalizedNosePosition
        );


        if (normalizedNosePosition
                < 0.5 - HEAD_THRESHOLD) {

            return "LOOKING_LEFT";
        }


        if (normalizedNosePosition
                > 0.5 + HEAD_THRESHOLD) {

            return "LOOKING_RIGHT";
        }


        return "FRONT";
    }


    // CALCULATE EYE GAZE
    private String calculateGazeDirection(
            Mat image,
            double rightEyeX,
            double rightEyeY,
            double leftEyeX,
            double leftEyeY
    ) {

        double eyeDistance =
                Math.abs(
                        leftEyeX - rightEyeX
                );


        if (eyeDistance < 10) {

            return "UNKNOWN";
        }


        // Estimate eye ROI size
        int eyeWidth =
                Math.max(
                        12,
                        (int) (
                                eyeDistance * 0.45
                        )
                );


        int eyeHeight =
                Math.max(
                        8,
                        (int) (
                                eyeDistance * 0.28
                        )
                );


        // Right pupil
        Double rightPupilPosition =
                detectPupilPosition(
                        image,
                        rightEyeX,
                        rightEyeY,
                        eyeWidth,
                        eyeHeight
                );


        // Left pupil
        Double leftPupilPosition =
                detectPupilPosition(
                        image,
                        leftEyeX,
                        leftEyeY,
                        eyeWidth,
                        eyeHeight
                );


        System.out.println(
                "Right Pupil Position: "
                        + rightPupilPosition
        );


        System.out.println(
                "Left Pupil Position: "
                        + leftPupilPosition
        );


        // Both eyes failed
        if (rightPupilPosition == null
                && leftPupilPosition == null) {

            return "UNKNOWN";
        }


        double averagePosition;


        // Only left eye available
        if (rightPupilPosition == null) {

            averagePosition =
                    leftPupilPosition;
        }

        // Only right eye available
        else if (leftPupilPosition == null) {

            averagePosition =
                    rightPupilPosition;
        }

        // Both eyes available
        else {

            averagePosition =
                    (
                            rightPupilPosition
                                    + leftPupilPosition
                    )
                            / 2.0;
        }


        System.out.println(
                "Average Pupil Position: "
                        + averagePosition
        );


        if (averagePosition
                < GAZE_LEFT_THRESHOLD) {

            return "GAZE_LEFT";
        }


        if (averagePosition
                > GAZE_RIGHT_THRESHOLD) {

            return "GAZE_RIGHT";
        }


        return "GAZE_CENTER";
    }


    // DETECT PUPIL POSITION
    private Double detectPupilPosition(
            Mat image,
            double eyeCenterX,
            double eyeCenterY,
            int eyeWidth,
            int eyeHeight
    ) {

        Rect eyeRect =
                createSafeEyeRect(
                        image,
                        eyeCenterX,
                        eyeCenterY,
                        eyeWidth,
                        eyeHeight
                );


        if (eyeRect == null) {

            return null;
        }


        Mat eyeRoi =
                new Mat(
                        image,
                        eyeRect
                );


        Mat gray =
                new Mat();


        Mat blurred =
                new Mat();


        try {

            // Convert eye ROI to grayscale
            Imgproc.cvtColor(
                    eyeRoi,
                    gray,
                    Imgproc.COLOR_BGR2GRAY
            );


            // Reduce noise
            Imgproc.GaussianBlur(
                    gray,
                    blurred,
                    new Size(5, 5),
                    0
            );


            if (blurred.empty()
                    || blurred.width() <= 0) {

                return null;
            }


            /*
             * Find darkest location.
             *
             * POC assumption:
             * pupil is one of the darkest
             * regions inside eye ROI.
             */
            Core.MinMaxLocResult result =
                    Core.minMaxLoc(
                            blurred
                    );


            Point darkestPoint =
                    result.minLoc;


            double normalizedPosition =
                    darkestPoint.x
                            / blurred.width();


            if (normalizedPosition < 0
                    || normalizedPosition > 1) {

                return null;
            }


            return normalizedPosition;

        } finally {

            blurred.release();
            gray.release();
            eyeRoi.release();
        }
    }


    // CREATE SAFE EYE RECTANGLE

    private Rect createSafeEyeRect(
            Mat image,
            double centerX,
            double centerY,
            int width,
            int height
    ) {

        int x =
                (int) (
                        centerX
                                - width / 2.0
                );


        int y =
                (int) (
                        centerY
                                - height / 2.0
                );


        x = Math.max(
                0,
                x
        );


        y = Math.max(
                0,
                y
        );


        int safeWidth =
                Math.min(
                        width,
                        image.width() - x
                );


        int safeHeight =
                Math.min(
                        height,
                        image.height() - y
                );


        if (safeWidth <= 2
                || safeHeight <= 2) {

            return null;
        }


        return new Rect(
                x,
                y,
                safeWidth,
                safeHeight
        );
    }
}