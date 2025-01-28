package com.college.receipt.controllers;

import com.college.receipt.entities.Ingredients;
import com.college.receipt.entities.Recipe;
import com.college.receipt.entities.Steps;
import com.college.receipt.entities.UploadedFile;
import com.college.receipt.exceptions.recipesNotFoundException;
import com.college.receipt.repositories.IngredientRepository;
import com.college.receipt.repositories.StepRepository;
import com.college.receipt.repositories.UploadedFileRepository;
import com.college.receipt.service.Recipe.RecipeRepository;
import com.college.receipt.service.Recipe.RecipeServiceImpl;
import com.college.receipt.service.UploadedFileService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.net.Authenticator;
import java.util.List;

@RequestMapping("/")
@Controller
public class RecipeController {
    private final RecipeRepository recipeRepository;
    private final UploadedFileService uploadedFileService;
    private final UploadedFileRepository uploadedFileRepository;
    public static final Logger logger = LoggerFactory.getLogger(RecipeController.class);
    private final RecipeServiceImpl recipeService;
    private final StepRepository stepRepository;
    private final IngredientRepository ingredientRepository;

    public RecipeController(RecipeRepository recipeRepository, UploadedFileService uploadedFileService, UploadedFileRepository uploadedFileRepository, RecipeServiceImpl recipeService, StepRepository stepRepository, IngredientRepository ingredientRepository) {
        this.recipeRepository = recipeRepository;
        this.uploadedFileService = uploadedFileService;
        this.uploadedFileRepository = uploadedFileRepository;
        this.recipeService = recipeService;
        this.stepRepository = stepRepository;
        this.ingredientRepository = ingredientRepository;
    }

    @GetMapping("/")
    public String home(Model model, String keyword,
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
            model.addAttribute("recipes", filteredRecipes);
            if (keyword != null){
                model.addAttribute("recipes", recipeRepository.findByKeyword(keyword));
                logger.info("поисковое слово:{}",keyword);
            }
            logger.info("Применяем фильтры: countPortion={}, kkal={}, timeToCook={}, nationalKitchen={}, restrictions={}, theme={}, typeOfCook={}, typeOfFood={}", countPortion, kkal, timeToCook, nationalKitchen, restrictions, theme, typeOfCook, typeOfFood);
            List<Recipe> recipes = recipeRepository.findByFilter( countPortion, kkal, timeToCook, nationalKitchen, restrictions, theme, typeOfCook, typeOfFood);
            logger.info("Найдено отфильтрованных рецептов: {}", recipes.size());
            if (recipes.size() == 0) {
                model.addAttribute("errorMessage", "Ошибка. Рецепты не были найдены");
                model.addAttribute("recipes", List.of());
                logger.warn("Ошибка, рецепты не найдены");
                return "index";
            }
            else{
                logger.info("Найдено рецептов: {}", recipes.size());
                recipes.forEach(recipe -> logger.info("Рецепт: id={}, name={}", recipe.getId(), recipe.getName()));
            }
        }
        else{
            List<Recipe> recipes = recipeRepository.findAll();
            model.addAttribute("recipes", recipes);
        }
        return "index";
    }

    @GetMapping("/create_recipe")
    public String createForm(Model model) {
        logger.info("Открыта страница создания рецептов");
        model.addAttribute("recipe", new Recipe());
        return "create_recipe";
    }
//
//    @PutMapping("/update_recipe/{id}")
//    public String updateRecipe(            @Valid @ModelAttribute Recipe recipe,
//                                           BindingResult result,
//                                           @PathVariable("id") Long id,
//                                           @RequestParam("photoFood") MultipartFile photoFood,
//                                           @RequestParam("stepPhotos") MultipartFile[] stepPhotos,
//                                           @RequestParam("stepDescriptions") String[] stepDescriptions,
//                                           @RequestParam("ingredientNames") String[] ingredientNames,
//                                           @RequestParam("ingredientsCounts") Integer[] ingredientsCounts,
//                                           Model model){
//
//        if(result.hasErrors()){
//            logger.error("Ошибка");
//            model.addAttribute("errorMessage", "Пожалуйста, заполните все обязательные поля.");
//            return "create_recipe";
//        }
//        try{
//            Recipe savedRecipe = recipeRepository.findRecipeById(id).orElseThrow(() -> {
//                logger.error("Рецепт с id={} не найден", id);
//                return new RuntimeException("Recipe not found");
//            });
//            savedRecipe.setDescription(recipe.getDescription());
//            savedRecipe.setName(recipe.getName());
//            savedRecipe.setKkal(recipe.getKkal());
//            savedRecipe.setRestrictions(recipe.getRestrictions());
//            savedRecipe.setTimeToCook(recipe.getTimeToCook());
//            savedRecipe.setCountPortion(recipe.getCountPortion());
//            savedRecipe.setTheme(recipe.getTheme());
//
//            Recipe updatedRecipe
//        }
//        catch (Exception e){
//            logger.error("Ошибка при обновлении рецепта: {}", e.getMessage());
//            model.addAttribute("errorMessage", "Ошибка при сохранении рецепта: " + e.getMessage());
//            return "update_recipe";
//        }
//        return "redirect:/recipe/" + id;
//    }

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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        if (result.hasErrors()) {
            logger.error("Ошибка валидации: {}", result.getAllErrors());
            model.addAttribute("errorMessage", "Пожалуйста, заполните все обязательные поля.");
            return "create_recipe";
        }
        try {
            Recipe savedRecipe = recipeService.createRecipe(recipe, photoFood, stepPhotos, stepDescriptions, userName, ingredientNames, ingredientsCounts);
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

    @GetMapping("/recipe/{id}")
    public String viewRecipe(@PathVariable("id") Long id, Model model) {
        logger.info("Открыта страница просмотра рецепта с id={}", id);

        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Рецепт с id={} не найден", id);
                    return new RuntimeException("Recipe not found");
                });

        List<UploadedFile> photoFood = uploadedFileRepository.findByRecipeAndIsPhotoFoodTrue(recipe);
        List<Steps> steps = stepRepository.findByRecipe(recipe);
        List<Ingredients> ingredients = ingredientRepository.findByRecipe(recipe);

        logger.info("Найдено шагов: {}", steps.size());

        model.addAttribute("recipe", recipe);
        model.addAttribute("photoFood", photoFood);
        model.addAttribute("steps", steps);
        model.addAttribute("ingredients", ingredients);

        return "recipe";
    }



    @DeleteMapping("/recipe/{id}/delete")
    public String deleteRecipe(@PathVariable("id") Long id) {
        Recipe recipe = recipeRepository.findById(id).orElseThrow(() -> new RuntimeException("Рецепт с id: " + id + " не найден"));
        recipeRepository.deleteById(id);
        logger.info("Рецепт {} удалён", recipe.getName());
        return "redirect:/";
    }


}
