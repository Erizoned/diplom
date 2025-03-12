package com.college.receipt;

import com.college.receipt.entities.Recipe;
import com.college.receipt.entities.Steps;
import com.college.receipt.entities.UploadedFile;
import com.college.receipt.entities.User;
import com.college.receipt.repositories.StepRepository;
import com.college.receipt.repositories.RecipeRepository;
import com.college.receipt.repositories.UploadedFileRepository;
import com.college.receipt.repositories.UserRepository;
import com.college.receipt.service.RecipeService;
import com.college.receipt.service.UploadedFileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class RecipeCreationTest {

    @InjectMocks
    private RecipeService recipeService;

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
        Integer[] ingredientCounts = {3, 400};

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

        Recipe savedRecipe = recipeService.createRecipe(recipe, photoFood,new MultipartFile[]{stepPhoto1, stepPhoto2}, stepDescription, ingredientNames, ingredientCounts);

        //Assert

        assertNotNull(savedRecipe);
        assertEquals("Тестовый рецепт", savedRecipe.getName());

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
