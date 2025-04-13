package com.college.receipt.repositories;

import com.college.receipt.entities.Ingredients;
import com.college.receipt.entities.Recipe;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IngredientRepository extends JpaRepository<Ingredients, Long> {
    List<Ingredients> findByRecipe(Recipe recipe);

    @Query(value = "SELECT * FROM ingredients i WHERE i.name LIKE %:keyword%", nativeQuery = true)
    List<Ingredients> findByKeyword(@Param("keyword") String keyword);

    void deleteByRecipeId(Long id);
}
