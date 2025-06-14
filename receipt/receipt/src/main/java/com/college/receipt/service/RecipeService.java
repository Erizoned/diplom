package com.college.receipt.service;

import com.college.receipt.entities.*;
import com.college.receipt.repositories.*;
import com.college.receipt.service.User.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.college.receipt.controllers.RecipeController.logger;

@Slf4j
@Service
@RequiredArgsConstructor
@Validated
@Transactional
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final UploadedFileService uploadedFileService;
    private final StepRepository stepRepository;
    private final UserRepository userRepository;
    private final IngredientRepository ingredientRepository;
    private final RatingRepository ratingRepository;
    private final UserService userService;
    private final DietRepository dietRepository;

    public Recipe createRecipe(Recipe recipe, MultipartFile photoFood, MultipartFile[] stepPhotos, String[] stepDescriptions, String[] ingredientNames , double[] ingredientsCounts, String[] units) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        if (userName == null){
            throw new RuntimeException("Ошибка. Пользователь не найден либо не авторизован");
        }
        User user = userRepository.findByEmail(userName);
        recipe.setCreatedBy(user);
        recipe.setRatings(null);
        recipe.setAvgRating(0.0);
        Recipe savedRecipe = recipeRepository.save(recipe);
        logger.info("Началась обработка рецепта:{}", recipe.getName());
        for (int i = 0; i < ingredientNames.length; i++){
            String unitValue = "";
            if (units != null && units.length > i) {
                unitValue = units[i];
            }
            Ingredients ingredient = Ingredients.builder()
                    .name(ingredientNames[i])
                    .count(ingredientsCounts[i])
                    .unit(unitValue)
                    .recipe(savedRecipe)
                    .build();
            savedRecipe.getIngredients().add(ingredient);
            logger.info("Сохранён ингредиент {} количеством {}", ingredientNames[i], ingredientsCounts[i]);
        }

        if (!photoFood.isEmpty()) {
            uploadedFileService.uploadImageToDataSystem(photoFood, recipe, "PHOTOFOOD");
            logger.info("Фото блюда {} успешно сохранено", photoFood.getOriginalFilename());
        }

        logger.info("Получено photoFood: {}", photoFood.getOriginalFilename());

        for (int i = 0; i < stepPhotos.length; i++) {
            MultipartFile stepPhoto = stepPhotos[i];
            String stepDescription = stepDescriptions[i];
            Integer stepNumber = i + 1;

            if (!stepPhoto.isEmpty()) {
                UploadedFile photo = uploadedFileService.uploadImageToDataSystem(stepPhoto, recipe,"STEPPHOTO");

                logger.info("Фото шага {} успешно сохранено: {}", stepNumber, stepPhoto.getOriginalFilename());

                Steps step = Steps.builder()
                        .stepNumber(stepNumber)
                        .description(stepDescription)
                        .photo(photo)
                        .recipe(recipe)
                        .build();
                stepRepository.save(step);
                logger.info("Шаг {} успешно сохранён: {}", stepNumber, stepDescription);
            } else {
                logger.warn("Фото для шага {} не передано.", stepNumber);
            }
        }
        logger.info("Рецепт {} пользователя {} успешно сохранён!", recipe.getName(), userName);
        return savedRecipe;
    }

    public Recipe updateRecipe(
            Long id,
            Recipe recipe,
            MultipartFile photoFood,
            MultipartFile[] stepPhotos,
            String[] stepDescriptions,
            String[] ingredientNames,
            double[] ingredientsCounts,
            String[] units
    ) throws IOException {
        Recipe savedRecipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Рецепт не найден. Айди рецепта:" + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        boolean isOwner = savedRecipe.getCreatedBy().getEmail().equals(currentUserEmail);
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"));

        if (!isAdmin && !isOwner) {
            throw new IllegalArgumentException("У пользователя недостаточно прав для редактирования рецепта");
        }

        savedRecipe.setName(recipe.getName());
        savedRecipe.setDescription(recipe.getDescription());
        savedRecipe.setKkal(recipe.getKkal());
        savedRecipe.setTypeOfCook(recipe.getTypeOfCook());
        savedRecipe.setTypeOfFood(recipe.getTypeOfFood());
        savedRecipe.setRestrictions(recipe.getRestrictions());
        savedRecipe.setTimeToCook(recipe.getTimeToCook());
        savedRecipe.setCountPortion(recipe.getCountPortion());
        savedRecipe.setTheme(recipe.getTheme());
        savedRecipe.setDefault(false);

        List<Steps> oldSteps = stepRepository.findByRecipeId(id);
        Map<Integer, UploadedFile> oldPhotosByStep = oldSteps.stream()
                .collect(Collectors.toMap(Steps::getStepNumber, Steps::getPhoto));

        ingredientRepository.deleteByRecipeId(id);
        stepRepository.deleteByRecipeId(id);

        if (ingredientNames != null && ingredientsCounts != null && ingredientNames.length == ingredientsCounts.length) {
            for (int i = 0; i < ingredientNames.length; i++) {
                String unitValue = "";
                if (units != null && units.length > i) {
                    unitValue = units[i];
                }
                Ingredients ingredient = Ingredients.builder()
                        .name(ingredientNames[i])
                        .count(ingredientsCounts[i])
                        .unit(unitValue)
                        .recipe(savedRecipe)
                        .build();
                savedRecipe.getIngredients().add(ingredient);
            }
        }

        if (photoFood != null && !photoFood.isEmpty()) {
            uploadedFileService.uploadImageToDataSystem(photoFood, savedRecipe, "PHOTOFOOD");
        }

        for (int j = 0; j < stepDescriptions.length; j++) {
            MultipartFile stepPhoto = (stepPhotos != null && stepPhotos.length > j) ? stepPhotos[j] : null;
            String stepDescription = stepDescriptions[j];
            Integer stepNumber = j + 1;

            UploadedFile photoEntity;
            if (stepPhoto != null && !stepPhoto.isEmpty()) {
                photoEntity = uploadedFileService.uploadImageToDataSystem(stepPhoto, savedRecipe, "STEPPHOTO");
            } else {
                photoEntity = oldPhotosByStep.get(stepNumber);
            }

            Steps newStep = Steps.builder()
                    .stepNumber(stepNumber)
                    .description(stepDescription)
                    .recipe(savedRecipe)
                    .photo(photoEntity)
                    .build();
            stepRepository.save(newStep);
        }

        recipeRepository.save(savedRecipe);
        return savedRecipe;
    }

    public ResponseEntity<String> deleteRecipe(Long id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findAuthenticatedUser();
        String currentUserEmail = user.getEmail();
        Recipe recipe = recipeRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Рецепт с id: " + id + " не найден"));
        boolean isOwner = recipe.getCreatedBy().getEmail().equals(currentUserEmail);
        boolean isAdmin = user.getRoles().stream().anyMatch(role -> "ADMIN".equals(role.getName()));
        if (!isAdmin && !isOwner){
            logger.warn("Пользователь {} попытался удалить рецепт {}, но у него недостаточно прав.", currentUserEmail, recipe.getName());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("У вас недостаточно прав для удаления рецепта");
        }
        List<Diet> allDiets = dietRepository.findAll();
        for (Diet diet : allDiets) {
            diet.getRecipesForBreakfast().removeIf(r -> r.getId().equals(id));
            diet.getRecipesForLunch().removeIf(r -> r.getId().equals(id));
            diet.getRecipesForDiner().removeIf(r -> r.getId().equals(id));
        }
        dietRepository.saveAll(allDiets);
        recipeRepository.deleteById(id);
        logger.info("Пользователь {} удалил рецепт {}", currentUserEmail, recipe.getName());

        return ResponseEntity.ok("Рецепт успешно удалён");
    }

    public List<Recipe> findAllRecipes() {
        List<Recipe> recipes = recipeRepository.findAll();
        System.out.println("Найдено рецептов: " + recipes.size()); // Временный вывод
        return recipes;
    }

    public List<Recipe> findByKeyword(String keyword){
        return recipeRepository.findByKeyword(keyword);
    }

    public List<Recipe> findByFilter(Integer countPortion, Integer kkal, Integer timeToCook, String nationalKitchen, String restrictions, String theme, String typeOfCook, String typeOfFood){
        return recipeRepository.findByFilter(null, countPortion, kkal, timeToCook, nationalKitchen, restrictions, theme, typeOfCook, typeOfFood);
    }

    public Recipe addNewRating(int value, Long recipeId){
        Recipe recipe = findRecipe(recipeId);
        User user = userService.findAuthenticatedUser();
        Rating rating = ratingRepository.findByUserAndRecipe(user, recipe).orElse(null);
        if (rating != null){
            logger.info("Пользователь уже ставил рейтинг, текущее значение: {} заменяется на новое: {}", rating.getRating(), value);
            rating.setRating(value);
        }
        else{
            logger.info("Пользователь {} впервые ставит рейтинг: {}", user.getUsername(), value);
            rating = new Rating();
            rating.setRating(value);
            rating.setUser(user);
            rating.setRecipe(recipe);
        }
        ratingRepository.save(rating);
        Double avgRating = ratingRepository.avgRating(recipeId);
        recipe.setAvgRating(avgRating);
        logger.info("Средний рейтинг рецепта теперь: {}", recipe.getAvgRating());
        return recipeRepository.save(recipe);
    }

    public Recipe findRecipe(Long id){
        return recipeRepository.findById(id).orElseThrow(() -> new RuntimeException("Рецепт не найден"));
    }

    public ResponseEntity<List<Recipe>> getRecipes(String keyword, Integer countPortion, Integer kkal, Integer timeToCook, String nationalKitchen, String restrictions, String theme, String typeOfCook, String typeOfFood) {
        if (keyword != null || countPortion != null || kkal != null || timeToCook != null ||
                nationalKitchen != null || restrictions != null || theme != null ||
                typeOfCook != null || typeOfFood != null) {
            logger.info("Фильтрация");
            List<Recipe> filteredRecipes = recipeRepository.findByFilter(
                    null,
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
                List<Recipe> keywordRecipe = recipeRepository.findByKeyword(keyword.toLowerCase());
                keywordRecipe = keywordRecipe.stream().filter(recipe -> !recipe.isDefault()).toList();
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
                filteredRecipes = filteredRecipes.stream().filter(recipe -> !recipe.isDefault()).toList();
                filteredRecipes.forEach(recipe -> logger.info("Рецепт: id={}, name={}", recipe.getId(), recipe.getName()));
                return ResponseEntity.ok(filteredRecipes);
            }
        }
        else{
            List<Recipe> recipes = recipeRepository.findAll();
            recipes = recipes.stream().filter(recipe -> !recipe.isDefault()).toList();
            return ResponseEntity.ok(recipes);
        }
    }
}
