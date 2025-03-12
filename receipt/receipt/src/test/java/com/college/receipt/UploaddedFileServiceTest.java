package com.college.receipt;

import com.college.receipt.entities.Recipe;
import com.college.receipt.entities.UploadedFile;
import com.college.receipt.repositories.RecipeRepository;
import com.college.receipt.repositories.StepRepository;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class UploaddedFileServiceTest {

    @Mock
    private RecipeService recipeService;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private UploadedFileRepository uploadedFileRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
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
    public void UploadedFileRepository_SavePhotos_ReturnSavedPhotos() throws IOException {
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

        String filePath = "C:/User/Anton/Documents/photos/" + photoFood.getOriginalFilename();

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

        Mockito.when(uploadedFileRepository.save(Mockito.any(UploadedFile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UploadedFile savedStepPhoto1 = uploadedFileService.uploadImageToDataSystem(stepPhoto1, recipe, "STEPPHOTO");
        UploadedFile savedStepPhoto2 = uploadedFileService.uploadImageToDataSystem(stepPhoto2, recipe, "STEPPHOTO");
        UploadedFile photofood = uploadedFileService.uploadImageToDataSystem(photoFood, recipe, "PHOTOFOOD");

        assertNotNull(savedStepPhoto1);
        assertNotNull(savedStepPhoto2);
        assertNotNull(photofood);
//        assertNotNull(savedStep1);
//        assertEquals(1, savedStep1.getStepNumber());
//        assertEquals("Нагреть сковороду", savedStep1.getDescription());
//        assertNotNull(savedStep2);
//        assertEquals(2, savedStep2.getStepNumber());
//        assertEquals("Добавить масло", savedStep2.getDescription());
    }

}
