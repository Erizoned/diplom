package service;

import com.college.receipt.Recipe;

import java.util.List;
import java.util.Optional;

public interface RecipeService {
    Recipe createRecipe(Recipe recipe);
    Optional<Recipe> findRecipeById(Long id);
    Recipe deleteRecipeById(Long id);
    List<Recipe> findAllRecipes();
    Recipe updateRecipe(Recipe recipe);
}
