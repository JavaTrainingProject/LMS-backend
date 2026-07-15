package com.example.LMS_Ai_Proctoring.service;

import com.example.LMS_Ai_Proctoring.dto.FaceAnalysisResult;

import jakarta.annotation.PostConstruct;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;

import org.opencv.imgcodecs.Imgcodecs;
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


    // HEAD DIRECTION THRESHOLDS

    private static final double HEAD_HORIZONTAL_THRESHOLD =
            0.15;

    private static final double HEAD_UP_THRESHOLD =
            0.42;

    private static final double HEAD_DOWN_THRESHOLD =
            0.68;


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

        try (var inputStream =
                     resource.getInputStream()) {

            Files.copy(
                    inputStream,
                    tempModelFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );
        }

        faceDetector =
                FaceDetectorYN.create(
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


    // ANALYZE FACE

    public FaceAnalysisResult analyzeFace(
            MultipartFile file
    ) throws IOException {

        if (
                file == null
                        ||
                        file.isEmpty()
        ) {

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

        Mat faces = new Mat();

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
                        "NO_FACE"
                );
            }


            // MULTIPLE FACES

            if (faceCount > 1) {

                return new FaceAnalysisResult(
                        faceCount,
                        "MULTIPLE_FACES"
                );
            }


            // FACE BOUNDING BOX

            double faceY =
                    faces.get(0, 1)[0];

            double faceHeight =
                    faces.get(0, 3)[0];


            // FACIAL LANDMARKS USED FOR HEAD DIRECTION

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

            double noseY =
                    faces.get(0, 9)[0];

            double rightMouthY =
                    faces.get(0, 11)[0];

            double leftMouthY =
                    faces.get(0, 13)[0];


            // HEAD DIRECTION

            String headDirection =
                    calculateHeadDirection(
                            rightEyeX,
                            rightEyeY,
                            leftEyeX,
                            leftEyeY,
                            noseX,
                            noseY,
                            rightMouthY,
                            leftMouthY,
                            faceY,
                            faceHeight
                    );


            // DEBUG LOGS

            System.out.println(
                    "================================="
            );

            System.out.println(
                    "Face Count: "
                            + faceCount
            );

            System.out.println(
                    "Head Direction: "
                            + headDirection
            );

            System.out.println(
                    "================================="
            );


            // FINAL RESULT

            return new FaceAnalysisResult(
                    faceCount,
                    headDirection
            );

        } finally {

            faces.release();
            image.release();
        }
    }


    // CALCULATE HEAD DIRECTION

    private String calculateHeadDirection(
            double rightEyeX,
            double rightEyeY,
            double leftEyeX,
            double leftEyeY,
            double noseX,
            double noseY,
            double rightMouthY,
            double leftMouthY,
            double faceY,
            double faceHeight
    ) {

        // HORIZONTAL ANALYSIS

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

        if (eyeDistance > 0) {

            double normalizedNoseX =
                    (noseX - minEyeX)
                            / eyeDistance;

            System.out.println(
                    "Normalized Nose X: "
                            + normalizedNoseX
            );

            if (
                    normalizedNoseX
                            <
                            0.5 - HEAD_HORIZONTAL_THRESHOLD
            ) {

                return "LOOKING_LEFT";
            }

            if (
                    normalizedNoseX
                            >
                            0.5 + HEAD_HORIZONTAL_THRESHOLD
            ) {

                return "LOOKING_RIGHT";
            }
        }


        // VERTICAL ANALYSIS

        double averageEyeY =
                (
                        rightEyeY
                                +
                                leftEyeY
                )
                        / 2.0;

        double averageMouthY =
                (
                        rightMouthY
                                +
                                leftMouthY
                )
                        / 2.0;

        double verticalFaceRange =
                averageMouthY
                        -
                        averageEyeY;

        if (verticalFaceRange <= 0) {

            return "FRONT";
        }

        double normalizedNoseY =
                (noseY - averageEyeY)
                        /
                        verticalFaceRange;

        System.out.println(
                "Normalized Nose Y: "
                        + normalizedNoseY
        );

        if (
                normalizedNoseY
                        <
                        HEAD_UP_THRESHOLD
        ) {

            return "LOOKING_UP";
        }

        if (
                normalizedNoseY
                        >
                        HEAD_DOWN_THRESHOLD
        ) {

            return "LOOKING_DOWN";
        }

        return "FRONT";
    }
}