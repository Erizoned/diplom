package com.college.receipt.repositories;

import com.college.receipt.entities.Rating;
import com.college.receipt.entities.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.college.receipt.entities.User;

import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.recipe.id = :recipeId")
    Double avgRating(@Param("recipeId") Long recipeId);
    Optional<Rating> findByUserAndRecipe(User userid, Recipe recipeId);
}
