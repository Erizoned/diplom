package com.college.receipt.repositories;

import com.college.receipt.entities.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    public Optional<Recipe> findByName(String name);
}
