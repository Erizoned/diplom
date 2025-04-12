package com.college.receipt.controllers;

import com.college.receipt.DTO.RecipeDto;
import com.college.receipt.entities.Ingredients;
import com.college.receipt.entities.Recipe;
import com.college.receipt.repositories.RecipeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ExternalRequestController {

    private static final Logger logger = LoggerFactory.getLogger(ExternalRequestController.class);

    @Autowired
    private RecipeRepository recipeRepository;

    @PostMapping("/gemini")
    public ResponseEntity<?> sendRequestToGemini(@RequestBody Map<String, String> request) throws IOException, InterruptedException {
        String prompt = request.get("prompt");
        logger.info("Получен промпт: {}", prompt);
        String scriptPath = Paths.get("scripts","gemini.py").toAbsolutePath().toString();
        String pythonPath = Paths.get("venv", "Scripts", "python.exe").toAbsolutePath().toString();
        ProcessBuilder pb = new ProcessBuilder(
                pythonPath, scriptPath, prompt
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();
        String json = new BufferedReader(new InputStreamReader(process.getInputStream()))
                .lines()
                .collect(Collectors.joining());

        logger.info("JSON для парсинга:\n{}", json);

        ObjectMapper mapper = new ObjectMapper();
        try {
            RecipeDto recipeDto = mapper.readValue(json, RecipeDto.class);

            List<Recipe> recipes = recipeRepository.findByFilter(
                    recipeDto.getRecipe().getCountPortion(),
                    recipeDto.getRecipe().getKkal(),
                    recipeDto.getRecipe().getTimeToCook(),
                    recipeDto.getRecipe().getNationalKitchen(),
                    recipeDto.getRecipe().getRestrictions(),
                    recipeDto.getRecipe().getTheme(),
                    recipeDto.getRecipe().getTypeOfCook(),
                    recipeDto.getRecipe().getTypeOfFood()
            );

            int exitCode = process.waitFor();

            logger.info("Скрипт завершился с кодом: {}", exitCode);

            logger.info("Найденные рецепты: {}", recipes);

            return ResponseEntity.ok(recipes);
        }catch (Exception e){
            logger.error("Ошибка при разборе JSON или фильтрации: ", e);
            return ResponseEntity.badRequest().body("Ошибка парсинга или фильтрации");
        }
    }
}
