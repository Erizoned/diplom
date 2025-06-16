package com.college.receipt.controllers;

import com.college.receipt.DTO.CommentDto;
import com.college.receipt.DTO.RatingDto;
import com.college.receipt.DTO.RecipeDto;
import com.college.receipt.entities.*;
import com.college.receipt.repositories.*;
import com.college.receipt.service.RecipeService;
import com.college.receipt.service.UploadedFileService;
import com.college.receipt.service.User.UserService;
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
import java.util.*;
import java.util.stream.Collectors;

@RequestMapping("/api")
@RestController
public class RecipeController {
    private final RecipeRepository recipeRepository;
    private final UploadedFileRepository uploadedFileRepository;
    public static final Logger logger = LoggerFactory.getLogger(RecipeController.class);
    private final RecipeService recipeService;
    private final StepRepository stepRepository;
    private final IngredientRepository ingredientRepository;
    private final RatingRepository ratingRepository;
    private final UserService userService;

    public RecipeController(RecipeRepository recipeRepository, UploadedFileRepository uploadedFileRepository, RecipeService recipeService, StepRepository stepRepository, IngredientRepository ingredientRepository, RatingRepository ratingRepository, UserService userService) {
        this.recipeRepository = recipeRepository;
        this.uploadedFileRepository = uploadedFileRepository;
        this.recipeService = recipeService;
        this.stepRepository = stepRepository;
        this.ingredientRepository = ingredientRepository;
        this.ratingRepository = ratingRepository;
        this.userService = userService;
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
       return recipeService.getRecipes(keyword, countPortion, kkal, timeToCook, nationalKitchen, restrictions, theme, typeOfCook, typeOfFood);
    }

    @PostMapping("/create_recipe")
    public ResponseEntity<?> createRecipe(
            @Valid @ModelAttribute Recipe recipe,
            BindingResult result,
            @RequestParam("photoFood") MultipartFile photoFood,
            @RequestParam("stepPhotos") MultipartFile[] stepPhotos,
            @RequestParam("stepDescriptions") String[] stepDescriptions,
            @RequestParam("ingredientNames") String[] ingredientNames,
            @RequestParam("ingredientsCounts") double[] ingredientsCounts,
            @RequestParam(value = "ingredientUnits", required = false) String[] units
    ){
        Recipe savedRecipe = null;
        if (result.hasErrors()) {
            logger.error("Ошибка валидации: {}", result.getAllErrors());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result.getAllErrors());
        }
        try {
            savedRecipe = recipeService.createRecipe(recipe, photoFood, stepPhotos, stepDescriptions, ingredientNames, ingredientsCounts, units);
            logger.info("Рецепт успешно сохранён: {}", savedRecipe);
        }
        catch (DataIntegrityViolationException e){
            return ResponseEntity.badRequest().body("Ошибка, количество слов превышает допустимый лимит. " + e.getMessage());
        }
        catch (IOException e){
            logger.error("Ошибка при сохранении рецепта: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        return ResponseEntity.ok(Map.of("id", savedRecipe.getId()));
    }


    @PutMapping("/update_recipe/{id}")
    public ResponseEntity<?> updateRecipe(
            @Valid @ModelAttribute Recipe recipe,
            BindingResult result,
            @PathVariable("id") Long id,
            @RequestParam(value = "photoFood", required = false) MultipartFile photoFood,
            @RequestParam(value = "stepPhotos", required = false) MultipartFile[] stepPhotos,
            @RequestParam(value = "stepDescriptions", required = false) String[] stepDescriptions,
            @RequestParam(value = "ingredientNames", required = false) String[] ingredientNames,
            @RequestParam(value = "ingredientsCounts", required = false) double[] ingredientsCounts,
            @RequestParam(value = "ingredientUnits", required = false) String[] units
            ) {
        logger.info("Попытка изменить рецепт");
        if (result.hasErrors()) {
            logger.error("Ошибка валидации: {}", result.getAllErrors());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result.getAllErrors());
        }
        try {
            Recipe updatedRecipe = recipeService.updateRecipe(id, recipe, photoFood, stepPhotos, stepDescriptions, ingredientNames, ingredientsCounts, units);
            return ResponseEntity.ok(updatedRecipe);
            } catch (Exception e) {
                logger.error("Внутренняя ошибка сервера: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Внутренняя ошибка сервера.");
            }
    }

    @GetMapping("/recipe/{id}")
    public ResponseEntity<?> viewRecipe(@PathVariable("id") Long id) {
        logger.info("Открыта страница просмотра рецепта с id={}", id);
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Рецепт с id={} не найден", id);
                    return new RuntimeException("Recipe not found");
                });
        if (recipe.isDefault()){
            return ResponseEntity.badRequest().body("Рецепт не существует");
        }
        UploadedFile photoFood = uploadedFileRepository.findByRecipeAndIsPhotoFoodTrue(recipe);
        List<Steps> steps = stepRepository.findByRecipe(recipe);
        List<Ingredients> ingredients = ingredientRepository.findByRecipe(recipe);

        logger.info("Найдено шагов: {}", steps.size());
        User user = userService.findAuthenticatedUser();
        List<CommentDto> commentsDtos = recipe.getComments().stream().map(CommentDto::new).collect(Collectors.toList());
        Rating rating = ratingRepository.findByUserAndRecipe(user, recipe).orElse(null);
        RecipeDto response = new RecipeDto(
          recipe,
          photoFood,
          steps,
          ingredients,
          recipe.getCreatedBy().getUsername(),
          commentsDtos,
          rating
        );

        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/recipe/{id}/rating")
    public ResponseEntity<Recipe> addRating(@PathVariable("id") Long id, @RequestBody RatingDto ratingDto){
        int rating = ratingDto.getRating();
        Recipe recipe = recipeService.addNewRating(rating, id);
        return ResponseEntity.ok().body(recipe);
    }


    @DeleteMapping("/recipe/{id}/delete")
    public ResponseEntity<String> deleteRecipe(@PathVariable("id") Long id) {
        return recipeService.deleteRecipe(id);
    }


}
