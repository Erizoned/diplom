package com.college.receipt.repositories;

import com.college.receipt.entities.Ingredients;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<Ingredients, Long> {
}
