package com.college.receipt.controllers;

import com.college.receipt.DTO.RecipeDto;
import com.college.receipt.entities.Diet;
import com.college.receipt.entities.Ingredients;
import com.college.receipt.entities.Recipe;
import com.college.receipt.entities.UploadedFile;
import com.college.receipt.repositories.IngredientRepository;
import com.college.receipt.repositories.RecipeRepository;
import com.college.receipt.service.DietService;
import com.college.receipt.service.RecipeService;
import com.college.receipt.service.UploadedFileService;
import com.college.receipt.service.User.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/script")
public class ExternalRequestController {

    private static final Logger logger = LoggerFactory.getLogger(ExternalRequestController.class);

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private DietService dietService;

    @Autowired
    private UserService userService;

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private UploadedFileService uploadedFileService;

    @PostMapping("/show_image")
    public ResponseEntity<?> showGeneratedImage(@RequestParam("image") MultipartFile file) throws IOException {
        UploadedFile newImage = uploadedFileService.uploadRegularFile(file);
        return ResponseEntity.ok(newImage);
    }

    @PostMapping("/create_image")
    public ResponseEntity<?> generateImage() throws IOException {
        String scriptPath = Paths.get("scripts", "generateImage.py").toAbsolutePath().toString();
        String pythonPath = Paths.get("venv", "Scripts", "python.exe").toAbsolutePath().toString();
        ProcessBuilder pb = new ProcessBuilder(
                pythonPath, scriptPath
        );
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

        int exitCode;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Задача была прервана во время исполнения скрипта:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.valueOf("text/plain; charset=UTF-8")).body("Скрипт прерван");
        }
        if (exitCode != 0) {
            logger.error("Скрипт питона завершился с кодом {}", exitCode);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.valueOf("text/plain; charset=UTF-8")).body("Ошибка скрипта, код ошибки: " + exitCode);
        }
        return ResponseEntity.ok().contentType(MediaType.valueOf("text/plain; charset=UTF-8")).body("Изображение успешно сгенерировано " + out);

    }

    @Value("${hf.token}")
    private String hfKey;

    @Value("${google.token}")
    private String googleKey;

    @Value("${unsplash.token}")
    private String unsplashKey;

    @PostMapping("/create_recipe")
    public ResponseEntity<?> createRecipe(@RequestBody Map<String, String> promptRequest, HttpServletRequest request) throws IOException {
        String prompt = promptRequest.get("prompt");
        logger.info("Получен промпт: {}", prompt);
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwtToken = authHeader.substring(7);
            String scriptPath = Paths.get("scripts", "recipeCreator.py").toAbsolutePath().toString();
            String pythonPath = Paths.get("venv", "Scripts", "python.exe").toAbsolutePath().toString();
            ProcessBuilder pb = new ProcessBuilder(
                    pythonPath, scriptPath, prompt
            );
            Map<String, String> env = pb.environment();
            env.put("UNSPLASH_ACCESS_KEY", unsplashKey);
            env.put("GOOGLE_API_KEY", googleKey);
            env.put("HF_TOKEN", hfKey);
            env.put("JWT_TOKEN", jwtToken);
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
            Pattern pattern = Pattern.compile("ID[:\\s]+(\\d+)");
            Matcher matcher  = pattern.matcher(out);
            Long recipeId = null;
            if (matcher.find()){
                recipeId = Long.parseLong(matcher.group(1));
                logger.info("Извлечён ID рецепта: {}", recipeId);
            }
            else {
                logger.error("Айди рецепта не найден!");
            }

            int exitCode;
            try {
                exitCode = process.waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Задача была прервана во время исполнения скрипта:", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.valueOf("text/plain; charset=UTF-8")).body("Скрипт прерван");
            }
            if (exitCode != 0) {
                logger.error("Скрипт питона завершился с кодом {}", exitCode);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.valueOf("text/plain; charset=UTF-8")).body("Ошибка скрипта, код ошибки: " + exitCode);
            }
            return ResponseEntity.ok().body(recipeId);
        }
        return ResponseEntity.badRequest().contentType(MediaType.valueOf("text/plain; charset=UTF-8")).body("Пользователь не авторизован");
    }

    @PostMapping("/diet")
    public ResponseEntity<?> createDiet(@RequestBody Map<String, String> request) throws IOException {
        String prompt = request.get("prompt");
        logger.info("Получен промпт: {}", prompt);
        String scriptPath = Paths.get("scripts","diet.py").toAbsolutePath().toString();
        String pythonPath = Paths.get("venv", "Scripts", "python.exe").toAbsolutePath().toString();
        ProcessBuilder pb = new ProcessBuilder(
                pythonPath, scriptPath, prompt
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null){
            logger.info("script: {}", line);
            response.append(line);
        }

        String answer = response.toString();

        String[] parts = answer.split("!");
        if (parts.length < 2){
            logger.error("Ошибка при создании рекомендаций. В ответе отсутствует восклицательный знак.");
            return ResponseEntity.badRequest().contentType(MediaType.valueOf("text/plain; charset=UTF-8")).body("Неверный формат ответа от скрипта.");
        }

        List<String> listOfRecipes = Arrays.stream(answer.split("!")[0].toLowerCase().split(",")).map(String::trim).filter(s ->!s.isEmpty()).toList();

        List<String> listOfRecipesBreakfast = listOfRecipes.stream().limit(3).toList();

        List<String> listOfRecipesLunch = listOfRecipes.stream().skip(3).limit(3).toList();

        List<String> listOfRecipesDiner = listOfRecipes.stream().skip(6).limit(3).toList();

        String recommendation = answer.split("!")[1];

        logger.info("Нейросеть предложила следующие рецепты на завтрак: {}, обед: {} и ужин: {}, с рекомендацией {}", listOfRecipesBreakfast, listOfRecipesLunch, listOfRecipesDiner, recommendation);

        List<Recipe> recipes = listOfRecipes.stream().map(recipeRepository::findByKeyword).filter(Objects::nonNull).flatMap(List::stream).toList();

        logger.info("Найдено {} рецептов для диеты: {}", recipes.size() , recipes);

        Diet diet = dietService.createDiet(listOfRecipesBreakfast, listOfRecipesLunch, listOfRecipesDiner, recommendation);

        return ResponseEntity.ok().body(diet);
    }

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

            String name = recipeDto.getRecipe().getName();
            if (name != null && !name.isBlank()) {
                name = "%" + name.trim() + "%";
                recipeDto.getRecipe().setName(name);
            }

            List<Recipe> recipes = recipeRepository.findByFilter(
                    recipeDto.getRecipe().getName(),
                    recipeDto.getRecipe().getCountPortion(),
                    recipeDto.getRecipe().getKkal(),
                    recipeDto.getRecipe().getTimeToCook(),
                    recipeDto.getRecipe().getNationalKitchen(),
                    recipeDto.getRecipe().getRestrictions(),
                    recipeDto.getRecipe().getTheme(),
                    recipeDto.getRecipe().getTypeOfCook(),
                    recipeDto.getRecipe().getTypeOfFood()
            );

            logger.info("Найденные рецепты до фильтрации: {}", recipes);

            if (recipes.stream().anyMatch(Recipe::hasOnlyName)){
                recipes = recipes.stream().map(recipe -> recipeRepository.findByKeyword(recipe.getName())).filter(Objects::nonNull).flatMap(List::stream).toList();
                logger.info("Найденные рецепты, где есть только имя: {}", recipes);
            }

            if (recipeDto.getIngredients() != null && !recipeDto.getIngredients().isEmpty()) {
                logger.info("Найдены рецепты с ингредиентами: {}", recipeDto.getIngredients());
                recipes = recipes.stream().filter(recipe -> recipe.getIngredients().stream().map(ing -> ing.getName().toLowerCase()).collect(Collectors.toSet()).containsAll(recipeDto.getIngredients().stream().map(ing -> ing.getName().toLowerCase()).collect(Collectors.toSet()))).collect(Collectors.toList());
                logger.info("Найденные рецепты с ингредиентами: {}", recipes);
            }
            else {
                logger.info("Найденные рецепты без ингредиентов: {}", recipes);
            }
            int exitCode = process.waitFor();

            logger.info("Скрипт завершился с кодом: {}", exitCode);

            return ResponseEntity.ok(recipes);
        }catch (Exception e){
            logger.error("Ошибка при разборе JSON или фильтрации: ", e);
            return ResponseEntity.badRequest().body("Ошибка парсинга или фильтрации");
        }
    }
}
