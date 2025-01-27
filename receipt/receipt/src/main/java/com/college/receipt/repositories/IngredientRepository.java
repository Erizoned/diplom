package com.college.receipt.repositories;

import com.college.receipt.entities.Ingredients;
import com.college.receipt.entities.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IngredientRepository extends JpaRepository<Ingredients, Long> {
    List<Ingredients> findByRecipe(Recipe recipe);
}
