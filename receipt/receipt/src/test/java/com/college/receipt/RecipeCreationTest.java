package com.college.receipt;

import com.college.receipt.entities.Recipe;
import com.college.receipt.entities.Steps;
import com.college.receipt.entities.UploadedFile;
import com.college.receipt.entities.User;
import com.college.receipt.repositories.*;
import com.college.receipt.service.RecipeService;
import com.college.receipt.service.UploadedFileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecipeCreationTest {

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private UploadedFileRepository uploadedFileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UploadedFileService uploadedFileService;

    @Mock
    private StepRepository stepRepository;

    @InjectMocks
    private RecipeService recipeService;

    @BeforeEach
    void setUp() {

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("erizoned@gmail.com", null)
        );

    }

    @Test
    public void RecipeRepository_SaveRecipe_ReturnSavedRecipe() throws IOException {

        MultipartFile photoFood = Mockito.mock(MultipartFile.class);
        MultipartFile stepPhoto1 = Mockito.mock(MultipartFile.class);
        MultipartFile stepPhoto2 = Mockito.mock(MultipartFile.class);

        String[] stepDescription = {"Нагреть сковороду","Добавить масло"};
        String[] ingredientNames = {"Яйца","Масло"};
        String[] units = {"шт","мл"};
        double[] ingredientCounts = {3, 400};

        Mockito.when(photoFood.getOriginalFilename()).thenReturn("photoFood.jpg");
        Mockito.when(stepPhoto1.getOriginalFilename()).thenReturn("stepPhoto1.jpg");
        Mockito.when(stepPhoto2.getOriginalFilename()).thenReturn("stepPhoto2.jpg");

        Recipe recipe = Recipe.builder()
                .id(1L)
                .name("Тестовый рецепт")
                .description("Тестовое описание")
                .ingredients(new ArrayList<>())
                .build();

        User user = User.builder()
                .username("Антон")
                .email("erizoned@gmail.com")
                .build();


        //Act

        Mockito.when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(userRepository.findByEmail(any())).thenReturn(user);

        Recipe savedRecipe = recipeService.createRecipe(recipe, photoFood,new MultipartFile[]{stepPhoto1, stepPhoto2}, stepDescription, ingredientNames, ingredientCounts, units);

        //Assert

        assertNotNull(savedRecipe);
        assertEquals("Тестовый рецепт", savedRecipe.getName());

    }

    @Test
    public void RecipeService_updateRecipe_returnUpdatedRecipe() throws IOException {

        MultipartFile photoFood = mock(MultipartFile.class);
        MultipartFile stepPhoto1 = mock(MultipartFile.class);
        MultipartFile stepPhoto2 = mock(MultipartFile.class);

        MultipartFile[] stepPhotos = {stepPhoto1, stepPhoto2};
        String[] stepDescriptions = {"Нагреть сковороду","Добавить масло"};
        String[] ingredientNames = {"Яйца","Масло"};
        double[] ingredientCounts = {3, 400};

        User user = User.builder()
                .username("Антон")
                .email("erizoned@gmail.com")
                .build();


        Recipe existingRecipe = Recipe.builder()
                .id(1L)
                .name("Тестовый рецепт")
                .description("Тестовое описание")
                .kkal(200)
                .typeOfCook("Жарка")
                .typeOfFood("Основное")
                .restrictions("Без глютена")
                .timeToCook(30)
                .countPortion(2)
                .theme("Домашний")
                .createdBy(user)
                .ingredients(new ArrayList<>())
                .build();

        Recipe updatedData = Recipe.builder()
                .name("Обновлённый рецепт")
                .description("Обновлённое описание")
                .kkal(250)
                .typeOfCook("Запекание")
                .typeOfFood("Десерт")
                .restrictions("Без сахара")
                .timeToCook(45)
                .countPortion(4)
                .theme("Праздничный")
                .build();

        String[] units = {"шт","мл"};
        //Act

        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(recipeRepository.findById(anyLong())).thenReturn(Optional.ofNullable(((existingRecipe))));

        Recipe savedRecipe = recipeService.updateRecipe(1L, updatedData, photoFood, stepPhotos, stepDescriptions, ingredientNames, ingredientCounts, units);

        // Assert
        assertNotNull(savedRecipe);
        assertEquals("Обновлённый рецепт", savedRecipe.getName());
        assertEquals("Обновлённое описание", savedRecipe.getDescription());
        assertEquals(250, savedRecipe.getKkal());
        assertEquals("Запекание", savedRecipe.getTypeOfCook());
        assertEquals("Десерт", savedRecipe.getTypeOfFood());
        assertEquals("Без сахара", savedRecipe.getRestrictions());
        assertEquals(45, savedRecipe.getTimeToCook());
        assertEquals(4, savedRecipe.getCountPortion());
        assertEquals("Праздничный", savedRecipe.getTheme());

        // Проверяем, что ингредиенты добавились корректно
        assertEquals(2, savedRecipe.getIngredients().size());
        assertEquals("Яйца", savedRecipe.getIngredients().get(0).getName());
        assertEquals(3, savedRecipe.getIngredients().get(0).getCount());
        assertEquals("Масло", savedRecipe.getIngredients().get(1).getName());
        assertEquals(400, savedRecipe.getIngredients().get(1).getCount());

        // Проверяем, что файлы загружались
        verify(uploadedFileService, times(1))
                .uploadImageToDataSystem(eq(photoFood), any(Recipe.class), eq("PHOTOFOOD"));
        verify(uploadedFileService, times(2))
                .uploadImageToDataSystem(any(MultipartFile.class), any(Recipe.class), eq("STEPPHOTO"));
        // Предполагаем, что для каждого шага вызывается сохранение
        verify(stepRepository, times(2)).save(any(Steps.class));
        // Проверяем, что удаление старых ингредиентов и шагов произошло
        verify(ingredientRepository, times(1)).deleteByRecipeId(1L);
        verify(stepRepository, times(1)).deleteByRecipeId(1L);
        // Проверяем, что рецепт сохраняется в репозитории
        verify(recipeRepository, times(1)).save(existingRecipe);
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
