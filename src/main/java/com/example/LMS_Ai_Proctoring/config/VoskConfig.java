package com.example.LMS_Ai_Proctoring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;;
import org.vosk.Model;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Configuration
public class VoskConfig {

    private static final String MODEL_RESOURCE_PATH = "models/vosk-model-small-en-us-0.15";

    @Bean
    public Model voskModel() throws IOException {
        File extractedModelDir = extractModelToTempDir();
        System.out.println("### VOSK MODEL LOADED FROM: " + extractedModelDir.getAbsolutePath());
        return new Model(extractedModelDir.getAbsolutePath());
    }

    /**
     * Copies the model folder from the classpath (works whether running
     * from IDE or a packaged jar) into a stable temp directory on disk,
     * then returns that directory. Skips re-extraction if already present.
     */
    private File extractModelToTempDir() throws IOException {
        File targetDir = new File(System.getProperty("java.io.tmpdir"), "vosk-model-small-en-us-0.15");

        // Already extracted from a previous run - reuse it
        if (targetDir.exists() && new File(targetDir, "am").exists()) {
            return targetDir;
        }

        targetDir.mkdirs();

        URL resourceUrl = getClass().getClassLoader().getResource(MODEL_RESOURCE_PATH);
        if (resourceUrl == null) {
            throw new IOException("Model not found on classpath at: " + MODEL_RESOURCE_PATH);
        }

        try {
            if (resourceUrl.getProtocol().equals("file")) {
                copyDirectory(new File(resourceUrl.toURI()), targetDir);
            } else if (resourceUrl.getProtocol().equals("jar")) {
                extractFromJar(resourceUrl, targetDir);
            } else {
                throw new IOException("Unsupported resource protocol: " + resourceUrl.getProtocol());
            }
        } catch (URISyntaxException e) {
            throw new IOException("Failed to resolve model resource URI", e);
        }

        return targetDir;
    }

    private void copyDirectory(File source, File target) throws IOException {
        Files.walk(source.toPath()).forEach(path -> {
            try {
                Path relative = source.toPath().relativize(path);
                Path destPath = target.toPath().resolve(relative);
                if (Files.isDirectory(path)) {
                    Files.createDirectories(destPath);
                } else {
                    Files.copy(path, destPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    private void extractFromJar(URL resourceUrl, File targetDir) throws IOException {
        String jarPath = resourceUrl.getPath().substring(5, resourceUrl.getPath().indexOf("!"));
        try (JarFile jarFile = new JarFile(URLDecoder(jarPath))) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().startsWith(MODEL_RESOURCE_PATH + "/")) {
                    String relativePath = entry.getName().substring((MODEL_RESOURCE_PATH + "/").length());
                    if (relativePath.isEmpty()) continue;

                    File outFile = new File(targetDir, relativePath);
                    if (entry.isDirectory()) {
                        outFile.mkdirs();
                    } else {
                        outFile.getParentFile().mkdirs();
                        try (InputStream in = jarFile.getInputStream(entry);
                             FileOutputStream out = new FileOutputStream(outFile)) {
                            in.transferTo(out);
                        }
                    }
                }
            }
        }
    }

    private String URLDecoder(String path) {
        try {
            return java.net.URLDecoder.decode(path, "UTF-8");
        } catch (Exception e) {
            return path;
        }
    }
}