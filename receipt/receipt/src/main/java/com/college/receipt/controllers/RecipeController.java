package com.college.receipt.controllers;

import com.college.receipt.entities.Recipe;
import com.college.receipt.entities.UploadedFile;
import com.college.receipt.service.RecipeService;
import com.college.receipt.service.UploadedFileService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.college.receipt.service.UploadedFileService;

import jakarta.validation.Valid;

import java.io.File;
import java.io.IOException;

@RequestMapping("/recipes")
@Controller
public class RecipeController {
    private final RecipeService recipeService;
    private final UploadedFileService uploadedFileService;

    public RecipeController(RecipeService recipeService, UploadedFileService uploadedFileService) {
        this.recipeService = recipeService;
        this.uploadedFileService = uploadedFileService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("recipes", recipeService.findAllRecipes());
        return "index";
    }

    @GetMapping("/create_recipe")
    public String createForm(Model model) {
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
        if (result.hasErrors()) {
            model.addAttribute("errorMessage", "Пожалуйста, заполните все обязательные поля.");
            return "create_recipe";
        }

        try {
            // Сохраняем рецепт сначала, чтобы получить его ID
            Recipe savedRecipe = recipeService.createRecipe(recipe);

            // Сохранение фото блюда
            if (!photoFood.isEmpty()) {
                UploadedFile photoFoodFile = new UploadedFile();
                photoFoodFile.setName(photoFood.getOriginalFilename());
                photoFoodFile.setType(photoFood.getContentType());
                photoFoodFile.setFilePath("C:/Users/Anton/Documents/photos/" + photoFood.getOriginalFilename());
                photoFoodFile.setRecipe(savedRecipe); // Связь с рецептом

                photoFood.transferTo(new File(photoFoodFile.getFilePath()));
                uploadedFileService.save(photoFoodFile);
            }

            // Сохранение фото шагов приготовления
            if (!stepPhoto.isEmpty()) {
                UploadedFile stepPhotoFile = new UploadedFile();
                stepPhotoFile.setName(stepPhoto.getOriginalFilename());
                stepPhotoFile.setType(stepPhoto.getContentType());
                stepPhotoFile.setFilePath("C:/Users/Anton/Documents/photos/" + stepPhoto.getOriginalFilename());
                stepPhotoFile.setRecipe(savedRecipe); // Связь с рецептом

                stepPhoto.transferTo(new File(stepPhotoFile.getFilePath()));
                uploadedFileService.save(stepPhotoFile);
            }
        } catch (IOException e) {
            model.addAttribute("errorMessage", "Ошибка при загрузке изображений: " + e.getMessage());
            return "create_recipe";
        }

        return "redirect:/recipes";
    }

    @GetMapping("/{id}/recipe")
    public String viewRecipe(@PathVariable("id") Long id, Model model) {
        Recipe recipe = recipeService.findRecipeById(id)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));
        model.addAttribute("recipe", recipe);
        return "recipe";
    }
//    @GetMapping("/{id}/recipe/photo")
//    @ResponseBody
//    public byte[] getRecipePhoto(@PathVariable("id") Long id) {
//        Recipe recipe = recipeService.findRecipeById(id)
//                .orElseThrow(() -> new RuntimeException("Recipe not found"));
//        return recipe.getPhotoFood(); // Проверьте, что изображение возвращается корректно
//    }

}
