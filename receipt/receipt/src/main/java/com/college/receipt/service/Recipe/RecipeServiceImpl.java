package com.college.receipt.service.Recipe;

import com.college.receipt.entities.Recipe;
import com.college.receipt.entities.UploadedFile;
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

    public Recipe createRecipe(Recipe recipe, MultipartFile photoFood, List<MultipartFile> stepPhotos) throws IOException {
        Recipe savedRecipe = recipeRepository.save(recipe);
        if (!photoFood.isEmpty()) {
            UploadedFile photoFoodFile = new UploadedFile();
            logger.info("Обработка фото блюда: {}");
            photoFoodFile.setName(photoFood.getOriginalFilename());
            photoFoodFile.setType(photoFood.getContentType());
            photoFoodFile.setFilePath("C:/Users/Anton/Documents/photos/" + photoFood.getOriginalFilename());
            photoFoodFile.setRecipe(savedRecipe);
            photoFoodFile.setPhotoFood(true);

            photoFood.transferTo(new File(photoFoodFile.getFilePath()));
            uploadedFileService.save(photoFoodFile);
            logger.info("Фото блюда {} успешно сохранено", photoFood.getOriginalFilename());
        }
        logger.info("Получено photoFood: {}", photoFood != null ? photoFood.getOriginalFilename() : "null");
        logger.info("Получено stepPhotos: {}", stepPhotos != null ? stepPhotos.size() : "null");
        logger.info("Получено фотографий шагов приготовления: {}", stepPhotos.size());
        if (stepPhotos != null && !stepPhotos.isEmpty()) {
            for (MultipartFile stepPhoto : stepPhotos) {
                logger.info("Файл: {}", stepPhoto);
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
            }
        }
        else {
            logger.warn("Шаги приготовления не переданы.");
            return savedRecipe;
        }

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
