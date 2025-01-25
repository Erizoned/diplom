package com.college.receipt;

import com.college.receipt.entities.Recipe;
import com.college.receipt.entities.UploadedFile;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class RecipeCreationTest {

    @Autowired
    private RecipeServiceImpl recipeService;

    @MockBean
    private RecipeRepository recipeRepository;

    @MockBean
    private UploadedFileService uploadedFileService;

    @Test
    public void RecipeRepository_SaveRecipe_ReturnSavedRecipe() throws IOException {
        //Arrange
        MultipartFile photoFood = Mockito.mock(MultipartFile.class);
        MultipartFile stepPhoto1 = Mockito.mock(MultipartFile.class);
        MultipartFile stepPhoto2 = Mockito.mock(MultipartFile.class);

        Mockito.when(photoFood.getOriginalFilename()).thenReturn("photoFood.jpg");
        Mockito.when(stepPhoto1.getOriginalFilename()).thenReturn("stepPhoto1.jpg");
        Mockito.when(stepPhoto2.getOriginalFilename()).thenReturn("stepPhoto2.jpg");

        Recipe recipe = Recipe.builder()
                .name("Тестовый рецепт")
                .description("Тестовое описание").build();

        //Act

        Mockito.when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Recipe savedRecipe = recipeService.createRecipe(recipe, photoFood, stepPhoto1);

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
