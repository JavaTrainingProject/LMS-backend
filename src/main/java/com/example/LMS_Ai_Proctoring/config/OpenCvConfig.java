package com.example.LMS_Ai_Proctoring.config;

import jakarta.annotation.PostConstruct;
import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenCvConfig {

    @PostConstruct
    public void loadOpenCV() {

        OpenCV.loadLocally();

        System.out.println("=================================");
        System.out.println("OpenCV loaded successfully");
        System.out.println("OpenCV Version: " + Core.VERSION);
        System.out.println("=================================");
    }
}