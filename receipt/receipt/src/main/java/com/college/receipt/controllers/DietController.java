package com.college.receipt.controllers;

import com.college.receipt.entities.Diet;
import com.college.receipt.entities.Recipe;
import com.college.receipt.repositories.DietRepository;
import com.college.receipt.repositories.RecipeRepository;
import com.college.receipt.service.DietService;
import com.college.receipt.service.RecipeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Paths;

@RestController
@RequestMapping("/api/diet")
public class DietController {

    @Autowired
    private DietRepository dietRepository;

    @Autowired
    private DietService dietService;

    @Autowired
    private RecipeRepository recipeRepository;

    private static final Logger logger = LoggerFactory.getLogger(DietController.class);

    @GetMapping("/{id}")
    public ResponseEntity<Diet> showDiet(@PathVariable("id") Long id){
        return ResponseEntity.ok().body(dietRepository.findById(id).orElseThrow(() -> new RuntimeException("Диета не найдена")));
    }

    @DeleteMapping("/delete/{id}")
    public void deleteDiet(@PathVariable("id") Long id){
        logger.info("Идёт удаление диеты");
        Diet diet = dietRepository.findById(id).orElseThrow(() -> new RuntimeException("Диета не найдена"));
        dietRepository.delete(diet);
    }

    @PostMapping("/create/recipe/default/{id}")
    public void changeDefaultRecipe(@PathVariable("id") Long id, @RequestParam String name){
        logger.info("Запрос на замену рецепта");
        dietService.changeDefaultRecipe(id, name);
    }
}
