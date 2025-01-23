package com.college.receipt.controllers;

import com.college.receipt.entities.Recipe;
import com.college.receipt.entities.UploadedFile;
import com.college.receipt.exceptions.recipesNotFoundException;
import com.college.receipt.repositories.UploadedFileRepository;
import com.college.receipt.service.Recipe.RecipeRepository;
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

    public RecipeController(RecipeRepository recipeRepository, UploadedFileService uploadedFileService, UploadedFileRepository uploadedFileRepository) {
        this.recipeRepository = recipeRepository;
        this.uploadedFileService = uploadedFileService;
        this.uploadedFileRepository = uploadedFileRepository;
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
            @RequestParam("stepPhoto") MultipartFile stepPhoto,
            Model model
    ) {
        logger.info("Метод createRecipe вызван с recipe={}, photoFood={}, stepPhoto={}", recipe, photoFood.getOriginalFilename(), stepPhoto.getOriginalFilename());

        if (result.hasErrors()) {
            logger.warn("Ошибка валидации: {}", result.getAllErrors());
            model.addAttribute("errorMessage", "Пожалуйста, заполните все обязательные поля.");
            return "create_recipe";
        }

        try {
            logger.info("Сохраняем рецепт: {}", recipe);
            Recipe savedRecipe = recipeRepository.save(recipe);
            logger.info("Рецепт успешно сохранён: {}", savedRecipe);

            if (!photoFood.isEmpty()) {
                logger.info("Обработка фото блюда: {}");
                UploadedFile photoFoodFile = new UploadedFile();
                photoFoodFile.setName(photoFood.getOriginalFilename());
                photoFoodFile.setType(photoFood.getContentType());
                photoFoodFile.setFilePath("C:/Users/Anton/Documents/photos/" + photoFood.getOriginalFilename());
                photoFoodFile.setRecipe(savedRecipe);
                photoFoodFile.setPhotoFood(true);

                photoFood.transferTo(new File(photoFoodFile.getFilePath()));
                uploadedFileService.save(photoFoodFile);
                logger.info("Фото блюда {} успешно сохранено", photoFood.getOriginalFilename());
            }

            if (!stepPhoto.isEmpty()) {
                logger.info("Обработка фото шага приготовления: {}");
                UploadedFile stepPhotoFile = new UploadedFile();
                stepPhotoFile.setName(stepPhoto.getOriginalFilename());
                stepPhotoFile.setType(stepPhoto.getContentType());
                stepPhotoFile.setFilePath("C:/Users/Anton/Documents/photos/" + stepPhoto.getOriginalFilename());
                stepPhotoFile.setRecipe(savedRecipe);
                stepPhotoFile.setPhotoFood(false);

                stepPhoto.transferTo(new File(stepPhotoFile.getFilePath()));
                uploadedFileService.save(stepPhotoFile);
                logger.info("Фото шага приготовления {} успешно сохранено", stepPhoto.getOriginalFilename());
            }
        } catch (IOException e) {
            logger.error("Ошибка при загрузке файлов: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Ошибка при загрузке изображений: " + e.getMessage());
            return "create_recipe";
        }

        logger.info("Рецепт {} успешно сохранён", recipe);
        return "redirect:/recipe/" + recipe.getId();
    }

    @GetMapping("/recipe/{id}")
    public String viewRecipe(@PathVariable("id") Long id, Model model) {
        logger.info("Открыта страница просмотра рецепта с id={}", id);
        Recipe recipe = recipeRepository.findRecipeById(id)
                .orElseThrow(() -> {
                    logger.error("Рецепт с id={} не найден", id);
                    return new RuntimeException("Recipe not found");
                });

        List<UploadedFile> uploadedFiles = recipe.getPhotos();
        logger.info("Найдено {} фотографий для рецепта id={}", uploadedFiles.size(), id);

        List<UploadedFile> photoFoods = uploadedFiles != null
                ? uploadedFiles.stream().filter(UploadedFile::isPhotoFood).toList()
                : List.of();
        List<UploadedFile> stepPhotos = uploadedFiles != null
                ? uploadedFiles.stream().filter(file -> !file.isPhotoFood()).toList()
                : List.of();

        logger.info("Фото блюда: {}. Фото шагов: {}", photoFoods.size(), stepPhotos.size());

        model.addAttribute("recipe", recipe);
        model.addAttribute("photoFoods", photoFoods);
        model.addAttribute("stepPhotos", stepPhotos);

        return "recipe";
    }
}
