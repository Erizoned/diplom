package com.college.receipt;

import com.college.receipt.config.SecurityConfig;
import com.college.receipt.entities.Recipe;
import com.college.receipt.entities.Steps;
import com.college.receipt.entities.UploadedFile;
import com.college.receipt.repositories.StepRepository;
import com.college.receipt.service.Recipe.RecipeRepository;
import com.college.receipt.service.Recipe.RecipeServiceImpl;
import com.college.receipt.service.UploadedFileService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
public class RecipeCreationTest {

    @Autowired
    private RecipeServiceImpl recipeService;

    @MockBean
    private RecipeRepository recipeRepository;

    @MockBean
    private UploadedFileService uploadedFileService;

    @MockBean
    private StepRepository stepRepository;

    @Test
    public void RecipeRepository_SaveRecipe_ReturnSavedRecipe() throws IOException {
        //Arrange
        String userName = "Anton@gmail.com";

        MultipartFile photoFood = Mockito.mock(MultipartFile.class);
        MultipartFile stepPhoto1 = Mockito.mock(MultipartFile.class);
        MultipartFile stepPhoto2 = Mockito.mock(MultipartFile.class);

        String[] stepDescription = {"Нагреть сковороду","Добавить масло"};
        String[] ingredientNames = {"Яйца","Масло"};
        Integer[] ingredientCounts = {3, 400};

        Mockito.when(photoFood.getOriginalFilename()).thenReturn("photoFood.jpg");
        Mockito.when(stepPhoto1.getOriginalFilename()).thenReturn("stepPhoto1.jpg");
        Mockito.when(stepPhoto2.getOriginalFilename()).thenReturn("stepPhoto2.jpg");
        Mockito.when(photoFood.getContentType()).thenReturn("image/jpeg");
        Mockito.when(stepPhoto1.getContentType()).thenReturn("image/jpeg");
        Mockito.when(stepPhoto2.getContentType()).thenReturn("image/jpeg");

        String filePath = "C:/Users/Anton/Documents/photos/" + photoFood.getOriginalFilename();


        Recipe recipe = Recipe.builder()
                .id(1L)
                .name("Тестовый рецепт")
                .description("Тестовое описание")
                .ingredients(new ArrayList<>())
                .build();


        //Act

        Mockito.when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(uploadedFileService.save(any(UploadedFile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(stepRepository.save(any(Steps.class))).thenAnswer(invocation -> invocation.getArgument(0));


        Recipe savedRecipe = recipeService.createRecipe(recipe, photoFood,new MultipartFile[]{stepPhoto1, stepPhoto2}, stepDescription, userName, ingredientNames, ingredientCounts);

        //Assert

        assertNotNull(savedRecipe);
        assertEquals("Тестовый рецепт", savedRecipe.getName());

        Mockito.verify(uploadedFileService, Mockito.times(1)).save(Mockito.argThat(file ->
                file.getName().equals("photoFood.jpg") &&
                        file.isPhotoFood()
        ));

        Mockito.verify(uploadedFileService, Mockito.times(1)).save(Mockito.argThat(file ->
                file.getName().equals("stepPhoto1.jpg") &&
                        !file.isPhotoFood()
        ));

        Mockito.verify(uploadedFileService, Mockito.times(1)).save(Mockito.argThat(file ->
                file.getName().equals("stepPhoto2.jpg") &&
                        !file.isPhotoFood()
        ));

        Mockito.verify(stepRepository, Mockito.times(2)).save(Mockito.any(Steps.class));


    }

    @Test
    public void UploadedFileRepository_SavePhotos_ReturnSavedPhotos() throws IOException{
        //Arrange
        MultipartFile photoFood = Mockito.mock(MultipartFile.class);
        MultipartFile stepPhoto1 = Mockito.mock(MultipartFile.class);
        MultipartFile stepPhoto2 = Mockito.mock(MultipartFile.class);

        String[] stepDescription = {"Нагреть сковороду","Добавить масло"};

        Mockito.when(photoFood.getOriginalFilename()).thenReturn("photoFood.jpg");
        Mockito.when(stepPhoto1.getOriginalFilename()).thenReturn("stepPhoto1.jpg");
        Mockito.when(stepPhoto2.getOriginalFilename()).thenReturn("stepPhoto2.jpg");
        Mockito.when(photoFood.getContentType()).thenReturn("image/jpeg");
        Mockito.when(stepPhoto1.getContentType()).thenReturn("image/jpeg");
        Mockito.when(stepPhoto2.getContentType()).thenReturn("image/jpeg");

        String filePath = "C:/Users/Anton/Documents/photos/" + photoFood.getOriginalFilename();

        Recipe recipe = Recipe.builder()
                .id(1L)
                .name("Тестовый рецепт")
                .description("Тестовое описание")
                .build();

        UploadedFile photoFoodFile = UploadedFile.builder()
                .name(photoFood.getOriginalFilename())
                .type(photoFood.getContentType())
                .filePath(filePath)
                .recipe(recipe)
                .isPhotoFood(true)
                .build();

        UploadedFile stepPhotoFile1 = UploadedFile.builder()
                .name(stepPhoto1.getOriginalFilename())
                .type(stepPhoto1.getContentType())
                .filePath(filePath)
                .recipe(recipe)
                .isPhotoFood(false)
                .build();

        UploadedFile stepPhotoFile2 = UploadedFile.builder()
                .name(stepPhoto2.getOriginalFilename())
                .type(stepPhoto2.getContentType())
                .filePath(filePath)
                .recipe(recipe)
                .isPhotoFood(false)
                .build();

        Steps step1 = Steps.builder()
                .stepNumber(1)
                .description(stepDescription[0])
                .photo(stepPhotoFile1)
                .recipe(recipe)
                .build();

        Steps step2 = Steps.builder()
                .stepNumber(2)
                .description(stepDescription[1])
                .photo(stepPhotoFile2)
                .recipe(recipe)
                .build();

        Mockito.when(uploadedFileService.save(any(UploadedFile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(stepRepository.save(any(Steps.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UploadedFile savedStepPhoto1 = uploadedFileService.save(stepPhotoFile1);
        UploadedFile savedStepPhoto2 = uploadedFileService.save(stepPhotoFile2);
        Steps savedStep1 = stepRepository.save(step1);
        Steps savedStep2 = stepRepository.save(step2);

        assertNotNull(savedStepPhoto1);
        assertNotNull(savedStepPhoto2);
        assertNotNull(savedStep1);
        assertEquals(1, savedStep1.getStepNumber());
        assertEquals("Нагреть сковороду", savedStep1.getDescription());
        assertNotNull(savedStep2);
        assertEquals(2, savedStep2.getStepNumber());
        assertEquals("Добавить масло", savedStep2.getDescription());
    }

    @Test
    public void RecipeRepository_GetAll_ReturnMoreThanOneRecipe(){
        //Arrange
        Recipe recipe1 = Recipe.builder()
                .name("Тестовый рецепт 1")
                .description("Тестовое описание").build();
        Recipe recipe2 = Recipe.builder()
                .name("Тестовый рецепт 2")
                .description("Тестовое описание").build();

        Mockito.when(recipeRepository.findAll()).thenReturn(List.of(recipe1, recipe2));

        //Act
        recipeRepository.save(recipe1);
        recipeRepository.save(recipe2);
        List<Recipe> recipeList = recipeRepository.findAll();

        //Assert
        assertNotNull(recipeList);
        assertEquals(2, recipeList.size());
        assertEquals("Тестовый рецепт 1", recipeList.get(0).getName());
        assertEquals("Тестовый рецепт 2", recipeList.get(1).getName());
    }
}
