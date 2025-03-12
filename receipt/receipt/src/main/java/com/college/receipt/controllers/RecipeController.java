package com.college.receipt.controllers;

import com.college.receipt.DTO.RecipeDto;
import com.college.receipt.entities.Ingredients;
import com.college.receipt.entities.Recipe;
import com.college.receipt.entities.Steps;
import com.college.receipt.entities.UploadedFile;
import com.college.receipt.repositories.IngredientRepository;
import com.college.receipt.repositories.StepRepository;
import com.college.receipt.repositories.UploadedFileRepository;
import com.college.receipt.repositories.RecipeRepository;
import com.college.receipt.service.RecipeService;
import com.college.receipt.service.UploadedFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@RequestMapping("/api")
@RestController
public class RecipeController {
    private final RecipeRepository recipeRepository;
    private final UploadedFileService uploadedFileService;
    private final UploadedFileRepository uploadedFileRepository;
    public static final Logger logger = LoggerFactory.getLogger(RecipeController.class);
    private final RecipeService recipeService;
    private final StepRepository stepRepository;
    private final IngredientRepository ingredientRepository;

    public RecipeController(RecipeRepository recipeRepository, UploadedFileService uploadedFileService, UploadedFileRepository uploadedFileRepository, RecipeService recipeService, StepRepository stepRepository, IngredientRepository ingredientRepository) {
        this.recipeRepository = recipeRepository;
        this.uploadedFileService = uploadedFileService;
        this.uploadedFileRepository = uploadedFileRepository;
        this.recipeService = recipeService;
        this.stepRepository = stepRepository;
        this.ingredientRepository = ingredientRepository;
    }

    @GetMapping("/recipes")
    public ResponseEntity<List<Recipe>> home(String keyword,
                       @RequestParam(value = "countPortion", required = false) Integer countPortion,
                       @RequestParam(value = "kkal", required = false) Integer kkal,
                       @RequestParam(value = "timeToCook", required = false) Integer timeToCook,
                       @RequestParam(value = "nationalKitchen", required = false) String nationalKitchen,
                       @RequestParam(value = "restrictions", required = false) String restrictions,
                       @RequestParam(value = "theme", required = false) String theme,
                       @RequestParam(value = "typeOfCook", required = false) String typeOfCook,
                       @RequestParam(value = "typeOfFood", required = false) String typeOfFood) {
        logger.info("Открыта главная страница со списком рецептов");
        if (keyword != null || countPortion != null || kkal != null || timeToCook != null ||
                nationalKitchen != null || restrictions != null || theme != null ||
                typeOfCook != null || typeOfFood != null) {
            logger.info("Фильтрация");
            List<Recipe> filteredRecipes = recipeRepository.findByFilter(
                    countPortion,
                    kkal,
                    timeToCook,
                    nationalKitchen,
                    restrictions,
                    theme,
                    typeOfCook,
                    typeOfFood
            );
            if (keyword != null){
                List<Recipe> keywordRecipe = recipeRepository.findByKeyword(keyword);
                logger.info("поисковое слово:{}", keyword);
                return ResponseEntity.ok(keywordRecipe);
            }
            logger.info("Применяем фильтры: countPortion={}, kkal={}, timeToCook={}, nationalKitchen={}, restrictions={}, theme={}, typeOfCook={}, typeOfFood={}", countPortion, kkal, timeToCook, nationalKitchen, restrictions, theme, typeOfCook, typeOfFood);
            logger.info("Найдено отфильтрованных рецептов: {}", filteredRecipes.size());
            if (filteredRecipes.isEmpty()) {
                logger.warn("Ошибка, рецепты не найдены");
                return ResponseEntity.badRequest().body(Collections.emptyList());
            }
            else{
                logger.info("Найдено рецептов: {}", filteredRecipes.size());
                filteredRecipes.forEach(recipe -> logger.info("Рецепт: id={}, name={}", recipe.getId(), recipe.getName()));
                return ResponseEntity.ok(filteredRecipes);
            }
        }
        else{
            List<Recipe> recipes = recipeRepository.findAll();
            return ResponseEntity.ok(recipes);
        }
    }

    @GetMapping("/create_recipe")
    public String createForm(Model model) {
        logger.info("Открыта страница создания рецептов");
        model.addAttribute("recipe", new Recipe());
        return "create_recipe";
    }

    @GetMapping("/update_recipe/{id}")
    public String updateRecipe(@PathVariable("id") Long id,Model model) {
        Recipe savedRecipe = recipeRepository.findRecipeById(id).orElseThrow(() -> {
            logger.error("Рецепт с id={} не найден", id);
            return new RuntimeException("Recipe not found");
        });

        UploadedFile photoFood = uploadedFileRepository.findByRecipeAndIsPhotoFoodTrue(savedRecipe);
        List<Steps> steps = stepRepository.findByRecipe(savedRecipe);
        List<Ingredients> ingredients = ingredientRepository.findByRecipe(savedRecipe);
        model.addAttribute("recipe", savedRecipe);
        model.addAttribute("steps", steps);
        model.addAttribute("ingredients", ingredients);
        model.addAttribute("photoFood", photoFood);

    return "update_recipe";
    }

    @PostMapping("/create_recipe")
    public String createRecipe(
            @Valid @ModelAttribute Recipe recipe,
            BindingResult result,
            @RequestParam("photoFood") MultipartFile photoFood,
            @RequestParam("stepPhotos") MultipartFile[] stepPhotos,
            @RequestParam("stepDescriptions") String[] stepDescriptions,
            @RequestParam("ingredientNames") String[] ingredientNames,
            @RequestParam("ingredientsCounts") Integer[] ingredientsCounts,
            Model model
    ){
        if (result.hasErrors()) {
            logger.error("Ошибка валидации: {}", result.getAllErrors());
            model.addAttribute("errorMessage", "Пожалуйста, заполните все обязательные поля.");
            throw new RuntimeException("Ошибка, заполнены не все поля");
        }
        try {
            Recipe savedRecipe = recipeService.createRecipe(recipe, photoFood, stepPhotos, stepDescriptions, ingredientNames, ingredientsCounts);
            logger.info("Рецепт успешно сохранён: {}", savedRecipe);
        }
        catch (DataIntegrityViolationException e){
            model.addAttribute("errorMessage","Ошибка, количество слов превышает допустимый лимит" + e.getMessage());
            return "create_recipe";
        }
        catch (IOException e){
            logger.error("Ошибка при сохранении рецепта: {}", e.getMessage());
            model.addAttribute("errorMessage", "Ошибка при сохранении рецепта: " + e.getMessage());
            return "create_recipe";
        }
        return "redirect:/recipe/" + recipe.getId();
    }


    @PutMapping("/update_recipe/{id}")
    public ResponseEntity<?> updateRecipe(
            @Valid @ModelAttribute Recipe recipe,
            BindingResult result,
            @PathVariable("id") Long id,
            @RequestParam("photoFood") MultipartFile photoFood,
            @RequestParam("stepPhotos") MultipartFile[] stepPhotos,
            @RequestParam("stepDescriptions") String[] stepDescriptions,
            @RequestParam("ingredientNames") String[] ingredientNames,
            @RequestParam("ingredientsCounts") Integer[] ingredientsCounts
            ) {
        if (result.hasErrors()) {
            logger.error("Ошибка валидации: {}", result.getAllErrors());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result.getAllErrors());
        }
        try {
            String errorMessage = recipeService.updateRecipe(id, recipe, photoFood, stepPhotos, stepDescriptions, ingredientNames, ingredientsCounts);
            if (errorMessage != null){
                logger.warn("Ошибка при обновлении рецепта: {}", errorMessage);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
            }
            logger.info("Рецепт успешно обновлён: {}", recipe.getName());
            return ResponseEntity.ok("Рецепт успешно обновлен.");
            } catch (Exception e) {
                logger.error("Внутренняя ошибка сервера: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Внутренняя ошибка сервера.");
            }
    }

    @GetMapping("/recipe/{id}")
    public ResponseEntity<RecipeDto> viewRecipe(@PathVariable("id") Long id) {
        logger.info("Открыта страница просмотра рецепта с id={}", id);
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Рецепт с id={} не найден", id);
                    return new RuntimeException("Recipe not found");
                });

        UploadedFile photoFood = uploadedFileRepository.findByRecipeAndIsPhotoFoodTrue(recipe);
        List<Steps> steps = stepRepository.findByRecipe(recipe);
        List<Ingredients> ingredients = ingredientRepository.findByRecipe(recipe);

        logger.info("Найдено шагов: {}", steps.size());

        RecipeDto response = new RecipeDto(
          recipe,
          photoFood,
          steps,
          ingredients,
          recipe.getCreatedBy().getUsername()
        );

        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/recipe/{id}/delete")
    public ResponseEntity<String> deleteRecipe(@PathVariable("id") Long id) {
        return recipeService.deleteRecipe(id);
    }


}
