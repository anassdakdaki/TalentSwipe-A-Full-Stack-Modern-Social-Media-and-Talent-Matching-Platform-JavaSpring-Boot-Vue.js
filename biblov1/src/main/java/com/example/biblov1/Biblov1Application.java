package com.example.biblov1;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Biblov1Application {

    public static void main(String[] args) {
        loadEnvLocal();
        SpringApplication.run(Biblov1Application.class, args);
    }

    private static void loadEnvLocal() {
        Path envFile = Paths.get(".env.local");
        if (!Files.isRegularFile(envFile)) {
            return;
        }

        try (Stream<String> lines = Files.lines(envFile, StandardCharsets.UTF_8)) {
            lines.map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .forEach(line -> {
                        int separatorIndex = line.indexOf('=');
                        if (separatorIndex <= 0) {
                            return;
                        }

                        String key = line.substring(0, separatorIndex).trim();
                        String value = line.substring(separatorIndex + 1).trim();
                        if (key.isEmpty()) {
                            return;
                        }

                        // Keep OS env vars and existing JVM properties as higher priority.
                        if (System.getenv(key) != null || System.getProperty(key) != null) {
                            return;
                        }

                        if ((value.startsWith("\"") && value.endsWith("\""))
                                || (value.startsWith("'") && value.endsWith("'"))) {
                            value = value.substring(1, value.length() - 1);
                        }

                        System.setProperty(key, value);
                    });
        } catch (IOException ex) {
            System.err.println("Could not read .env.local: " + ex.getMessage());
        }
    }

}
