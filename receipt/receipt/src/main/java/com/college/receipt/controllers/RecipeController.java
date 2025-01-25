package com.college.receipt.controllers;

import com.college.receipt.entities.Recipe;
import com.college.receipt.entities.Steps;
import com.college.receipt.entities.UploadedFile;
import com.college.receipt.exceptions.recipesNotFoundException;
import com.college.receipt.repositories.StepRepository;
import com.college.receipt.repositories.UploadedFileRepository;
import com.college.receipt.service.Recipe.RecipeRepository;
import com.college.receipt.service.Recipe.RecipeServiceImpl;
import com.college.receipt.service.UploadedFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.File;
import java.io.IOException;
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

    public RecipeController(RecipeRepository recipeRepository, UploadedFileService uploadedFileService, UploadedFileRepository uploadedFileRepository, RecipeServiceImpl recipeService, StepRepository stepRepository) {
        this.recipeRepository = recipeRepository;
        this.uploadedFileService = uploadedFileService;
        this.uploadedFileRepository = uploadedFileRepository;
        this.recipeService = recipeService;
        this.stepRepository = stepRepository;
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

    @PostMapping("/create_recipe")
    public String createRecipe(
            @Valid @ModelAttribute Recipe recipe,
            BindingResult result,
            @RequestParam("photoFood") MultipartFile photoFood,
            @RequestParam("stepPhotos") MultipartFile[] stepPhotos,
            @RequestParam("stepDescriptions") String[] stepDescriptions,
            Model model
    ) {
        logger.info("Получено фото блюда: {}", photoFood.getOriginalFilename());

        if (result.hasErrors()) {
            logger.error("Ошибка валидации: {}", result.getAllErrors());
            model.addAttribute("errorMessage", "Пожалуйста, заполните все обязательные поля.");
            return "create_recipe";
        }

        try {
            // Сохранение рецепта
            Recipe savedRecipe = recipeRepository.save(recipe);

            // Сохранение фото блюда
            savePhoto(photoFood, savedRecipe, true);

            // Сохранение шагов
            for (int i = 0; i < stepPhotos.length; i++) {
                saveStep(i + 1, stepDescriptions[i], stepPhotos[i], savedRecipe);
            }

            logger.info("Рецепт успешно сохранён: {}", savedRecipe);

        } catch (IOException e) {
            logger.error("Ошибка при загрузке файлов: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Ошибка при загрузке изображений: " + e.getMessage());
            return "create_recipe";
        }

        return "redirect:/recipe/" + recipe.getId();
    }

    private void savePhoto(MultipartFile file, Recipe recipe, boolean isPhotoFood) throws IOException {
        String filePath = "C:/Users/Anton/Documents/photos/" + file.getOriginalFilename();
        file.transferTo(new File(filePath));

        UploadedFile uploadedFile = UploadedFile.builder()
                .name(file.getOriginalFilename())
                .type(file.getContentType())
                .filePath(filePath)
                .recipe(recipe)
                .isPhotoFood(true)
                .build();

        uploadedFileService.save(uploadedFile);
    }

    private void saveStep(int stepNumber, String stepDescription, MultipartFile stepPhoto, Recipe recipe) throws IOException {
        if (!stepPhoto.isEmpty()) {
            // Сохранение фото шага
            UploadedFile stepPhotoFile = new UploadedFile();
            stepPhotoFile.setName(stepPhoto.getOriginalFilename());
            stepPhotoFile.setType(stepPhoto.getContentType());
            stepPhotoFile.setFilePath("C:/Users/Anton/Documents/photos/" + stepPhoto.getOriginalFilename());
            stepPhotoFile.setRecipe(recipe);
            stepPhotoFile.setPhotoFood(false); // Это фото шага, не блюда
            stepPhoto.transferTo(new File(stepPhotoFile.getFilePath()));
            uploadedFileService.save(stepPhotoFile);

            logger.info("Фото шага {} успешно сохранено: {}", stepNumber, stepPhoto.getOriginalFilename());

            // Сохранение шага с описанием
            Steps step = new Steps();
            step.setStepNumber(stepNumber);
            step.setDescription(stepDescription);
            step.setPhoto(stepPhotoFile); // Привязываем фото к шагу
            step.setRecipe(recipe); // Привязываем шаг к рецепту
            stepRepository.save(step);

            logger.info("Шаг {} успешно сохранён: {}", stepNumber, stepDescription);
        } else {
            logger.warn("Фото для шага {} не передано.", stepNumber);
        }
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

        logger.info("Найдено шагов: {}", steps.size());

        model.addAttribute("recipe", recipe);
        model.addAttribute("photoFood", photoFood);
        model.addAttribute("steps", steps);

        return "recipe";
    }
}
