package com.college.receipt.repositories;

import com.college.receipt.entities.Recipe;
import com.college.receipt.entities.Steps;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StepRepository extends JpaRepository<Steps, Long> {

    List<Steps> findByRecipe(Recipe recipe);
}
