package com.college.receipt.service;

import com.college.receipt.controllers.ExternalRequestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);

    public StringBuilder startScript(String prompt, String scriptName, Long id, String jwtToken, String dietName) throws IOException {
        String scriptPath = Paths.get("scripts", scriptName).toAbsolutePath().toString();
        String pythonPath = Paths.get("venv", "Scripts", "python.exe").toAbsolutePath().toString();
        List<String> command = new ArrayList<>();
        command.add(pythonPath);
        command.add(scriptPath);
        command.add(prompt);
        if (id != null && dietName != null && !dietName.isEmpty()){
            command.add(id.toString());
            command.add(dietName);
        }
        ProcessBuilder pb = new ProcessBuilder(command);
        Map<String, String> env = pb.environment();
        if (jwtToken != null){
            env.put("JWT_TOKEN", jwtToken);
        }
        pb.redirectErrorStream(true);
        Process process = pb.start();
        StringBuilder out = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
        )) {
            String line;
            while ((line = reader.readLine()) != null) {
                logger.info("script: {}", line);
                out.append(line).append("\n");
            }
        }
        int exitCode = 0;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Задача была прервана во время исполнения скрипта:", e);
        }
        if (exitCode != 0) {
            logger.error("Ошибка: {}", exitCode);
        }
        return out;
    }

}
