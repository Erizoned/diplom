package com.college.receipt.controllers;

import com.college.receipt.DTO.RecipeDto;
import com.college.receipt.entities.Ingredients;
import com.college.receipt.entities.Recipe;
import com.college.receipt.repositories.RecipeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ExternalRequestController {

    private static final Logger logger = LoggerFactory.getLogger(ExternalRequestController.class);

    @Autowired
    private RecipeRepository recipeRepository;

    @PostMapping("/preferences")
    public ResponseEntity<?> parsePreferences(@RequestBody RecipeDto data){
        logger.info("Полученны данные: {}", data);

        // Добавить сортировку по ингредиентам тоже
        List<Ingredients> ingredients = data.getIngredients();

        Integer kkal = data.getRecipe().getKkal();
        String theme = data.getRecipe().getTheme();
        String typeOfFood = data.getRecipe().getTypeOfFood();
        String typeOfCook = data.getRecipe().getTypeOfCook();
        String restrictions = data.getRecipe().getRestrictions();
        Integer countPortion = data.getRecipe().getCountPortion();
        String nationalKitchen = data.getRecipe().getNationalKitchen();
        Integer timeToCook = data.getRecipe().getTimeToCook();
        logger.info("Рецепт для фильтра: {}", data);
        List<Recipe> filteredRecipes = recipeRepository.findByFilter(
                countPortion,
                kkal,
                timeToCook,
                nationalKitchen,
                restrictions,
                theme,
                typeOfCook,
                typeOfFood
        );

        logger.info("Найденные рецепты {}", filteredRecipes);
        return ResponseEntity.ok().body(filteredRecipes);
    }
}
