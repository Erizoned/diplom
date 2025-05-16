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
        Diet diet = dietRepository.findByRecipe(oldRecipe);
        logger.info("Обновление рецепта {} с id {} на id {}", oldRecipe.getName(), oldRecipe.getId(), newRecipe.getId());
        boolean found = false;

        if (diet.getRecipesForBreakfast().contains(oldRecipe)) {
            diet.getRecipesForBreakfast().remove(oldRecipe);
            diet.getRecipesForBreakfast().add(newRecipe);
            found = true;
        }

        if (diet.getRecipesForLunch().contains(oldRecipe)) {
            diet.getRecipesForLunch().remove(oldRecipe);
            diet.getRecipesForLunch().add(newRecipe);
            found = true;
        }

        if (diet.getRecipesForDiner().contains(oldRecipe)) {
            diet.getRecipesForDiner().remove(oldRecipe);
            diet.getRecipesForDiner().add(newRecipe);
            found = true;
        }

        if (!found) {
            throw new RuntimeException("Рецепт не найден ни в одном из приёмов пищи");
        }

        recipeRepository.delete(oldRecipe);
        dietRepository.save(diet);
    }

    public List<Recipe> searchRecipeInList(List<String> recipeList){

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
            }
            else{
                return found.stream();
            }
        }).toList();
    }

    public Diet createDiet(List<String> recipeListB, List<String> recipeListL, List<String> recipeListD, String rec){

        List<Recipe> recipeListBreakfast = searchRecipeInList(recipeListB);
        List<Recipe> recipeListLunch = searchRecipeInList(recipeListL);
        List<Recipe> recipeListDinner = searchRecipeInList(recipeListD);

        User user = userService.findAuthenticatedUser();
        Diet diet = Diet.builder()
                .recipesForBreakfast(recipeListBreakfast)
                .recipesForLunch(recipeListLunch)
                .recipesForDiner(recipeListDinner)
                .recommendation(rec)
                .user(user)
                .term(LocalDate.now().plusDays(30))
                .dateOfCreation(LocalDate.now())
                .build();

        logger.info("Диета успешно создана: {}, {}, {}", diet.getRecipesForBreakfast(), diet.getRecipesForLunch(), diet.getRecipesForDiner());
        return dietRepository.save(diet);
    }
}
