package com.college.receipt.service;

import com.college.receipt.controllers.ExternalRequestController;
import com.college.receipt.entities.Diet;
import com.college.receipt.entities.Recipe;
import com.college.receipt.entities.User;
import com.college.receipt.repositories.DietRepository;
import com.college.receipt.repositories.RecipeRepository;
import com.college.receipt.service.User.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DietService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalRequestController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private DietRepository dietRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    public void updateRecipeInDiet(Long recipeId, Long newRecipeId){
        Recipe oldRecipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RuntimeException("Рецепт не найден"));
        Recipe newRecipe = recipeRepository.findById(newRecipeId).orElseThrow(() -> new RuntimeException("Рецепт не найден"));
        List<Diet> diets = dietRepository
                .findAllByRecipesForBreakfastContainsOrRecipesForLunchContainsOrRecipesForDinerContains(
                        oldRecipe, oldRecipe, oldRecipe
                );
        logger.info("Обновление рецепта {} с id {} на id {}", oldRecipe.getName(), oldRecipe.getId(), newRecipe.getId());
        if (diets.isEmpty()) {
            throw new RuntimeException("Рецепт не найден ни в одной диете");
        }

        for (Diet diet : diets) {
            boolean replaced = false;

            if (diet.getRecipesForBreakfast().remove(oldRecipe)) {
                diet.getRecipesForBreakfast().add(newRecipe);
                replaced = true;
            }
            if (diet.getRecipesForLunch().remove(oldRecipe)) {
                diet.getRecipesForLunch().add(newRecipe);
                replaced = true;
            }
            if (diet.getRecipesForDiner().remove(oldRecipe)) {
                diet.getRecipesForDiner().add(newRecipe);
                replaced = true;
            }

            if (replaced) {
                dietRepository.save(diet);
            }
        }
        recipeRepository.delete(oldRecipe);
    }
    public Diet createDiet(List<String> recipeKeysB, List<String> recipeKeysL, List<String> recipeKeysD, String recommendation) {
        List<Recipe> recipeListBreakfast = searchRecipeInList(recipeKeysB)
                .stream()
                .distinct()
                .limit(3)
                .collect(Collectors.toList());

        List<Recipe> recipeListLunch = searchRecipeInList(recipeKeysL)
                .stream()
                .distinct()
                .limit(3)
                .collect(Collectors.toList());

        List<Recipe> recipeListDinner = searchRecipeInList(recipeKeysD)
                .stream()
                .distinct()
                .limit(3)
                .collect(Collectors.toList());

        User user = userService.findAuthenticatedUser();

        Diet diet = Diet.builder()
                .recipesForBreakfast(recipeListBreakfast)
                .recipesForLunch(recipeListLunch)
                .recipesForDiner(recipeListDinner)
                .recommendation(recommendation)
                .user(user)
                .term(LocalDate.now().plusDays(30))
                .dateOfCreation(LocalDate.now())
                .build();

        return dietRepository.save(diet);
    }

    public List<Recipe> searchRecipeInList(List<String> recipeList) {
        return recipeList.stream().flatMap(keyword -> {
            List<Recipe> found = recipeRepository.findByKeyword(keyword);
            if (found == null || found.isEmpty()) {
                logger.info("Рецепт из списка диеты не найден, создаётся заглушка...");
                Recipe r = new Recipe();
                r.setName(keyword);
                r.setDescription("default");
                r.setTheme("default");
                r.setDefault(true);
                Recipe saved = recipeRepository.save(r);
                return Stream.of(saved);
            } else {
                return found.stream();
            }
        }).collect(Collectors.toList());
    }
}
