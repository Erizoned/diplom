package com.college.receipt.repositories;

import com.college.receipt.entities.Diet;
import com.college.receipt.entities.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface DietRepository extends JpaRepository<Diet, Long> {
    @Query("SELECT d FROM Diet d WHERE :recipe MEMBER OF d.recipesForBreakfast OR :recipe MEMBER OF d.recipesForLunch OR :recipe MEMBER OF d.recipesForDiner")
    Diet findByRecipe(@Param("recipe") Recipe recipe);
}
