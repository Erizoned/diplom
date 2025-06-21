package com.college.receipt.controllers;

import com.college.receipt.DTO.IngredientDto;
import com.college.receipt.DTO.RecipeDto;
import com.college.receipt.entities.Diet;
import com.college.receipt.entities.Ingredients;
import com.college.receipt.entities.Recipe;
import com.college.receipt.entities.UploadedFile;
import com.college.receipt.repositories.DietRepository;
import com.college.receipt.repositories.IngredientRepository;
import com.college.receipt.repositories.RecipeRepository;
import com.college.receipt.service.DietService;
import com.college.receipt.service.GeminiService;
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
    private DietRepository dietRepository;

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

    @Autowired
    private GeminiService geminiService;

    @PostMapping("/show_image")
    public ResponseEntity<?> showGeneratedImage(@RequestParam("image") MultipartFile file) throws IOException {
        UploadedFile newImage = uploadedFileService.uploadRegularFile(file);
        return ResponseEntity.ok(newImage);
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
            StringBuilder out = geminiService.startScript(prompt, "recipeCreator.py", null, jwtToken, null, null, null, null);
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
            if (recipeId != null){
                logger.info("Логика добавления рецепта в диету");
                Recipe newRecipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RuntimeException("Рецепт не найден"));
                List<Recipe> oldRecipeList = recipeRepository.findByName(newRecipe.getName());
                Recipe oldRecipe = oldRecipeList.stream().min(Comparator.comparing(Recipe::getId)).orElse(null);
                if (oldRecipe != null && !Objects.equals(oldRecipe.getId(), recipeId)){
                    logger.info("Старый рецепт найден. Замена на новый.");
                    dietService.updateRecipeInDiet(oldRecipe.getId(), recipeId);
                }
            }
            return ResponseEntity.ok().body(recipeId);
        }
        return ResponseEntity.badRequest().contentType(MediaType.valueOf("text/plain; charset=UTF-8")).body("Пользователь не авторизован");
    }


    @PostMapping("/diet")
    public ResponseEntity<?> createDiet(@RequestBody Map<String, String> request) throws IOException {
        String prompt = request.get("prompt");
        StringBuilder response = geminiService.startScript(prompt, "diet.py", null, null, null, null, null, null);
        String answer = response.toString();
        String[] parts = answer.split("!");
        if (parts.length < 3) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.valueOf("text/plain; charset=UTF-8"))
                    .body("Неверный формат ответа от скрипта.");
        }
        List<String> allKeys = Arrays.stream(parts[0].toLowerCase().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        List<String> keysBreakfast = allKeys.stream().limit(3).collect(Collectors.toList());
        List<String> keysLunch     = allKeys.stream().skip(3).limit(3).collect(Collectors.toList());
        List<String> keysDinner    = allKeys.stream().skip(6).limit(3).collect(Collectors.toList());

        String recommendation = parts[1];
        String name = parts[2];
        logger.info("Создаётся диета с названием {} и рекомендацией: {}", name, recommendation);
        Diet diet = dietService.createDiet(keysBreakfast, keysLunch, keysDinner, recommendation, name);
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

    @PostMapping("/diet/{dietId}/default/{id}")
    public void createNewDefaultRecipe(@PathVariable("dietId") Long dietId, @PathVariable("id") Long id,@RequestBody Map<String, String> prompt, HttpServletRequest request) throws IOException {
        String recipeName = prompt.get("recipeName");
        logger.info("Айди диеты: {}", dietId);
        Diet diet = dietRepository.findById(dietId).orElseThrow(() -> new RuntimeException("диета не найдена"));
        logger.info("Получен промпт: {}. Создаётся новый дефолтный рецепт для диеты: {}", recipeName, diet.getName());
        Recipe recipe = recipeRepository.findById(id).orElseThrow(() -> new RuntimeException("Рецепт не найден"));
        String recipeRole = diet.getRecipeRoleInDiet(recipe, diet);
        List<Recipe> recipes = new ArrayList<>();
        switch (recipeRole) {
            case "завтрак" -> recipes = diet.getRecipesForBreakfast();
            case "обед" -> recipes = diet.getRecipesForLunch();
            case "ужин" -> recipes = diet.getRecipesForDiner();
            default -> throw new IllegalStateException("Неизвестная роль: " + recipeRole);
        }
        String listOfRecipes = String.join(", ", recipes.stream().map(Recipe::getName).toList());
        logger.info("Рецепт находится в разделе {} в списке рецептов: {}", recipeRole, listOfRecipes);
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwtToken = authHeader.substring(7);
            geminiService.startScript(recipeName, "dietRecipes.py", id, jwtToken, diet.getName(), recipeName, recipeRole, listOfRecipes);
        }
        else {
            logger.error("Ошибка. Пользователь не авторизован");
        }
    }

    @PostMapping("/create_recipe_from_ingredients")
    public ResponseEntity<?> createRecipeFromIngredients(@RequestBody Map<String, List<IngredientDto>> body) throws IOException {
        List<IngredientDto> ing = body.get("ingredients");
        String prompt = ing.stream().map(IngredientDto::getName).collect(Collectors.joining(","));
        logger.info("Попытка создать рецепт из следующих ингредиентов:{}", prompt);
        StringBuilder out = geminiService.startScript(prompt, "recipeFromIngredients.py", null, null, null, null, null, null);
        String recipes = out.toString();
        logger.info("Вывод скрипта: {}", recipes);
        if (!recipes.contains("Errno") && !recipes.contains("Traceback")){
            logger.info("Создаются заглушки для рецептов: {}",recipes);
            List<String> listOfRecipes = Arrays.stream(recipes.split(",")).toList();
            List<Recipe> defaultRecipes = recipeService.createDefaultRecipes(listOfRecipes);
            return ResponseEntity.ok().body(defaultRecipes);
        }
        else {
            logger.error("В скрипте произошла ошибка");
            return ResponseEntity.badRequest().body("Произошла Ошибка");
        }
    }
}
