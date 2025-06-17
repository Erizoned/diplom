package com.college.receipt.repositories;

import com.college.receipt.entities.Diet;
import com.college.receipt.entities.Recipe;
import com.college.receipt.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DietRepository extends JpaRepository<Diet, Long> {
    @Query("SELECT d FROM Diet d WHERE :recipe MEMBER OF d.recipesForBreakfast OR :recipe MEMBER OF d.recipesForLunch OR :recipe MEMBER OF d.recipesForDiner")
    Diet findByRecipe(@Param("recipe") Recipe recipe);
    @Query("SELECT d FROM Diet d WHERE :recipe MEMBER OF d.recipesForBreakfast OR :recipe MEMBER OF d.recipesForLunch OR :recipe MEMBER OF d.recipesForDiner")
    List<Diet> findAllByRecipe(@Param("recipe") Recipe recipe);
    List<Diet> findAllByRecipesForBreakfastContainsOrRecipesForLunchContainsOrRecipesForDinerContains(
            Recipe breakfastRecipe,
            Recipe lunchRecipe,
            Recipe dinerRecipe
    );
    List<Diet> findByUser(User user);
}
