package com.college.receipt.service;

import com.college.receipt.entities.*;
import com.college.receipt.repositories.*;
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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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
    private final UploadedFileRepository uploadedFileRepository;

    public Recipe createRecipe(Recipe recipe, MultipartFile photoFood, MultipartFile[] stepPhotos, String[] stepDescriptions, String[] ingredientNames , double[] ingredientsCounts) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        if (userName == null){
            throw new RuntimeException("Ошибка. Пользователь не найден либо не авторизован");
        }
        User user = userRepository.findByEmail(userName);
        recipe.setCreatedBy(user);
        Recipe savedRecipe = recipeRepository.save(recipe);
        logger.info("Началась обработка рецепта:{}", recipe.getName());
        for (int i = 0; i < ingredientNames.length; i++){
            Ingredients ingredient = Ingredients.builder()
                    .name(ingredientNames[i])
                    .count(ingredientsCounts[i])
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
            double[] ingredientsCounts
    ) throws IOException {
        logger.info("Айди изменяемого рецепта:{}", id);

        Recipe savedRecipe = recipeRepository.findById(id).orElseThrow(() -> new RuntimeException("Рецепт не найден. Айди рецепта:" + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        boolean isOwner = savedRecipe.getCreatedBy().getEmail().equals(currentUserEmail);
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"));

        if(!isAdmin && !isOwner){
            logger.warn("Пользователь {} неудачно попытался изменить рецепт: {}", currentUserEmail, recipe.getName());
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

        ingredientRepository.deleteByRecipeId(id);
        stepRepository.deleteByRecipeId(id);

        if (ingredientNames != null && ingredientsCounts != null && ingredientNames.length == ingredientsCounts.length) {
            for (int i = 0; i < ingredientNames.length; i++) {
                Ingredients ingredient = Ingredients.builder()
                        .name(ingredientNames[i])
                        .count(ingredientsCounts[i])
                        .recipe(savedRecipe)
                        .build();
                savedRecipe.getIngredients().add(ingredient);
            }
        }

        if (photoFood != null && !photoFood.isEmpty()) {
            uploadedFileService.uploadImageToDataSystem(photoFood, savedRecipe, "PHOTOFOOD");
        }

        if (stepDescriptions != null && stepPhotos != null && stepDescriptions.length == stepPhotos.length) {
            for (int j = 0; j < stepPhotos.length; j++) {
                MultipartFile stepPhoto = stepPhotos[j];
                String stepDescription = stepDescriptions[j];
                Integer stepNumber = j + 1;

                if (!stepPhoto.isEmpty()) {
                    UploadedFile savedFile = uploadedFileService.uploadImageToDataSystem(stepPhoto, savedRecipe, "STEPPHOTO");

                    Steps step = Steps.builder()
                            .stepNumber(stepNumber)
                            .description(stepDescription)
                            .recipe(savedRecipe)
                            .photo(savedFile)
                            .build();
                    stepRepository.save(step);
                }
            }
        }
        recipeRepository.save(savedRecipe);
        logger.info("Рецепт {} с id {} успешно изменился",savedRecipe.getName(), savedRecipe.getId());
        return savedRecipe;
    }

    public ResponseEntity<String> deleteRecipe(Long id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        Recipe recipe = recipeRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Рецепт с id: " + id + " не найден"));
        boolean isOwner = recipe.getCreatedBy().getEmail().equals(currentUserEmail);
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (!isAdmin && !isOwner){
            logger.warn("Пользователь {} попытался удалить рецепт {}, но у него недостаточно прав.", currentUserEmail, recipe.getName());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("У вас недостаточно прав для удаления рецепта");
        }
        recipeRepository.deleteById(id);
        logger.info("Пользователь {} удалил рецепт {}", currentUserEmail, recipe.getName());

        return ResponseEntity.ok("Рецепт успешно удалён");
    }

    public List<Recipe> findAllRecipes() {
        List<Recipe> recipes = recipeRepository.findAll();
        System.out.println("Найдено рецептов: " + recipes.size()); // Временный вывод
        return recipes;
    }

    public Recipe updateRecipe(Recipe recipe){
        return recipeRepository.save(recipe);
    }

    public List<Recipe> findByKeyword(String keyword){
        return recipeRepository.findByKeyword(keyword);
    }

    public List<Recipe> findByFilter(Integer countPortion, Integer kkal, Integer timeToCook, String nationalKitchen, String restrictions, String theme, String typeOfCook, String typeOfFood){
        return recipeRepository.findByFilter(null, countPortion, kkal, timeToCook, nationalKitchen, restrictions, theme, typeOfCook, typeOfFood);
    }

}
