package com.college.receipt.service.Recipe;

import com.college.receipt.entities.*;
import com.college.receipt.repositories.IngredientRepository;
import com.college.receipt.repositories.StepRepository;
import com.college.receipt.repositories.UserRepository;
import com.college.receipt.service.UploadedFileService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.college.receipt.controllers.RecipeController.logger;

@Service
@RequiredArgsConstructor
@Validated
@Transactional
public class RecipeServiceImpl {

    private final RecipeRepository recipeRepository;
    private final UploadedFileService uploadedFileService;
    private final StepRepository stepRepository;
    private final UserRepository userRepository;
    private final IngredientRepository ingredientRepository;

    public Recipe createRecipe(Recipe recipe, MultipartFile photoFood, MultipartFile[] stepPhotos, String[] stepDescriptions, String userName, String[] ingredientNames , Integer[] ingredientsCounts) throws IOException {
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
            String filePath = "C:/Users/Anton/Documents/photos" + photoFood.getOriginalFilename();
            photoFood.transferTo(new File(filePath));

            UploadedFile uploadedFile = UploadedFile.builder()
                    .name(photoFood.getOriginalFilename())
                    .type(photoFood.getContentType())
                    .filePath(filePath)
                    .recipe(recipe)
                    .isPhotoFood(true)
                    .build();

            uploadedFileService.save(uploadedFile);
            logger.info("Фото блюда {} успешно сохранено", photoFood.getOriginalFilename());
        }
        logger.info("Получено photoFood: {}", photoFood != null ? photoFood.getOriginalFilename() : "null");
        for (int i = 0; i < stepPhotos.length; i++) {
            MultipartFile stepPhoto = stepPhotos[i];
            String stepDescription = stepDescriptions[i];
            Integer stepNumber = i + 1;

            if (!stepPhoto.isEmpty()) {
                UploadedFile stepPhotoFile = new UploadedFile();
                stepPhotoFile.setName(stepPhoto.getOriginalFilename());
                stepPhotoFile.setType(stepPhoto.getContentType());
                stepPhotoFile.setFilePath("C:/Users/Anton/Documents/photos" + stepPhoto.getOriginalFilename());
                stepPhotoFile.setRecipe(recipe);
                stepPhotoFile.setPhotoFood(false);
                stepPhoto.transferTo(new File(stepPhotoFile.getFilePath()));
                uploadedFileService.save(stepPhotoFile);

                logger.info("Фото шага {} успешно сохранено: {}", stepNumber, stepPhoto.getOriginalFilename());

                Steps step = new Steps();
                step.setStepNumber(stepNumber);
                step.setDescription(stepDescription);
                step.setPhoto(stepPhotoFile);
                step.setRecipe(recipe);
                stepRepository.save(step);

                logger.info("Шаг {} успешно сохранён: {}", stepNumber, stepDescription);
            } else {
                logger.warn("Фото для шага {} не передано.", stepNumber);
            }
        }
        logger.info("Рецепт {} пользователя {} успешно сохранён!", recipe.getName(), userName);
        return savedRecipe;
    }

    public Optional<Recipe> findRecipeById(Long id){
        return recipeRepository.findById(id);
    }

    public Recipe deleteRecipeById(Long id){
        Recipe recipe = findRecipeById(id).orElseThrow(() -> new NoSuchElementException("Recipe not found with id: " + id));
        recipeRepository.delete(recipe);
        return recipe;
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
        return recipeRepository.findByFilter(countPortion, kkal, timeToCook, nationalKitchen, restrictions, theme, typeOfCook, typeOfFood);
    }

}
