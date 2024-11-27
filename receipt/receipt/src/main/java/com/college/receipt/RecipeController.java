package com.college.receipt;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;

@RequestMapping("/recipes")
@Controller
public class RecipeController {
    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping("/test_page")
    public String testPage() {
        return "test_page";
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
            @RequestParam("stepPhoto") MultipartFile stepPhoto
    ) throws IOException {
        if (result.hasErrors()) {
            return "create_recipe";
        }

        // Сохранение фото блюда, если файл не пуст
        if (!photoFood.isEmpty()) {
            recipe.setPhotoFood(photoFood.getBytes());
        }

        // Сохранение фото шагов приготовления, если файл не пуст
        if (!stepPhoto.isEmpty()) {
            recipe.setStepPhoto(stepPhoto.getBytes());
        }

        recipeService.createRecipe(recipe);
        return "redirect:/recipes";
    }

    @GetMapping("/{id}/recipe")
    public String viewRecipe(@PathVariable("id") Long id, Model model) {
        Recipe recipe = recipeService.findRecipeById(id)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));
        model.addAttribute("recipe", recipe);
        return "recipe";
    }
}
