package com.college.receipt.service;

import com.college.receipt.entities.Recipe;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import com.college.receipt.repositories.RecipeRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Validated
@Transactional
public class RecipeServiceImpl implements RecipeService {
    private final RecipeRepository recipeRepository;

    @Override
    public Recipe createRecipe(Recipe recipe){
        return recipeRepository.save(recipe);
    }

    @Override
    public Optional<Recipe> findRecipeById(Long id){
        return recipeRepository.findById(id);
    }

    public Recipe deleteRecipeById(Long id){
        Recipe recipe = findRecipeById(id).orElseThrow(() -> new NoSuchElementException("Recipe not found with id: " + id));
        recipeRepository.delete(recipe);
        return recipe;
    }

    @Override
    public List<Recipe> findAllRecipes() {
        List<Recipe> recipes = recipeRepository.findAll();
        System.out.println("Найдено рецептов: " + recipes.size()); // Временный вывод
        return recipes;
    }

    public Recipe updateRecipe(Recipe recipe){
        return recipeRepository.save(recipe);
    }
}
