package com.example.LMS_Ai_Proctoring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.vosk.Model;

@Configuration
public class VoskConfig {

    @Bean
    public Model voskModel() throws Exception {

        ClassPathResource resource =
                new ClassPathResource("models/vosk-model-small-en-us-0.15");

        return new Model(resource.getFile().getAbsolutePath());
    }

}