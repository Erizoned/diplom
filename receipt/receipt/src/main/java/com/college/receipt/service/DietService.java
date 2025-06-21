package com.college.receipt.service;

import com.college.receipt.controllers.ExternalRequestController;
import com.college.receipt.entities.Diet;
import com.college.receipt.entities.Recipe;
import com.college.receipt.entities.User;
import com.college.receipt.repositories.DietRepository;
import com.college.receipt.repositories.RecipeRepository;
import com.college.receipt.service.User.UserService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DietService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalRequestController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private DietRepository dietRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    public void updateRecipeInDiet(Long recipeId, Long newRecipeId){
        Recipe oldRecipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RuntimeException("Рецепт не найден"));
        Recipe newRecipe = recipeRepository.findById(newRecipeId).orElseThrow(() -> new RuntimeException("Рецепт не найден"));
        List<Diet> diets = dietRepository
                .findAllByRecipesForBreakfastContainsOrRecipesForLunchContainsOrRecipesForDinerContains(
                        oldRecipe, oldRecipe, oldRecipe
                );
        logger.info("Обновление рецепта {} с id {} на id {}", oldRecipe.getName(), oldRecipe.getId(), newRecipe.getId());
        if (diets.isEmpty()) {
            throw new RuntimeException("Рецепт не найден ни в одной диете");
        }

        for (Diet diet : diets) {
            boolean replaced = false;

            if (diet.getRecipesForBreakfast().remove(oldRecipe)) {
                diet.getRecipesForBreakfast().add(newRecipe);
                replaced = true;
            }
            if (diet.getRecipesForLunch().remove(oldRecipe)) {
                diet.getRecipesForLunch().add(newRecipe);
                replaced = true;
            }
            if (diet.getRecipesForDiner().remove(oldRecipe)) {
                diet.getRecipesForDiner().add(newRecipe);
                replaced = true;
            }

            if (replaced) {
                dietRepository.save(diet);
            }
        }
        recipeRepository.delete(oldRecipe);
    }
    
    public Diet createDiet(List<String> recipeKeysB, List<String> recipeKeysL, List<String> recipeKeysD, String recommendation, String name) {
        List<Recipe> recipeListBreakfast = searchRecipeInList(recipeKeysB)
                .stream()
                .distinct()
                .limit(3)
                .collect(Collectors.toList());

        List<Recipe> recipeListLunch = searchRecipeInList(recipeKeysL)
                .stream()
                .distinct()
                .limit(3)
                .collect(Collectors.toList());

        List<Recipe> recipeListDinner = searchRecipeInList(recipeKeysD)
                .stream()
                .distinct()
                .limit(3)
                .collect(Collectors.toList());

        User user = userService.findAuthenticatedUser();

        Diet diet = Diet.builder()
                .name(name)
                .recipesForBreakfast(recipeListBreakfast)
                .recipesForLunch(recipeListLunch)
                .recipesForDiner(recipeListDinner)
                .recommendation(recommendation)
                .user(user)
                .term(LocalDate.now().plusDays(30))
                .dateOfCreation(LocalDate.now())
                .build();

        return dietRepository.save(diet);
    }

    public List<Recipe> searchRecipeInList(List<String> recipeList) {
        return recipeList.stream().flatMap(keyword -> {
            List<Recipe> found = recipeRepository.findByKeyword(keyword);
            if (found == null || found.isEmpty()) {
                logger.info("Рецепт из списка диеты не найден, создаётся заглушка...");
                Recipe saved = createDefaultRecipe(keyword);
                return Stream.of(saved);
            } else {
                return found.stream();
            }
        }).collect(Collectors.toList());
    }

    public Recipe createDefaultRecipe(String name){
        Recipe r = new Recipe();
        r.setName(name);
        r.setDescription("default");
        r.setTheme("default");
        r.setDefault(true);
        return recipeRepository.save(r);
    }

    private String determineRecipeRole(Diet d, Recipe recipe) {
        Long id = recipe.getId();
        if (d.getRecipesForBreakfast().stream().anyMatch(r -> r.getId().equals(id))) {
            return "завтрак";
        }
        if (d.getRecipesForLunch().stream().anyMatch(r -> r.getId().equals(id))) {
            return "обед";
        }
        if (d.getRecipesForDiner().stream().anyMatch(r -> r.getId().equals(id))) {
            return "ужин";
        }
        return null;
    }


    public void changeDefaultRecipe(Long recipeId, String name) {
        Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RuntimeException("Рецепт не найден"));
        List<Diet> diets = dietRepository.findAllByRecipe(recipe);
        Recipe newRecipe = recipeRepository.findByName(name).stream().filter(r -> !r.getId().equals(recipeId))
                .findFirst()
                .orElseGet(() -> createDefaultRecipe(name));
        for (Diet d : diets) {
            String role = determineRecipeRole(d, recipe);
            if (role == null) {
                logger.error("Рецепт {} не нашёлся ни в одном рационе диеты: {}", recipe.getName(), d.getName());
                continue;
            }
            List<Recipe> listToEdit;
            switch (role) {
                case "завтрак" -> listToEdit = d.getRecipesForBreakfast();
                case "обед" -> listToEdit = d.getRecipesForLunch();
                case "ужин" -> listToEdit = d.getRecipesForDiner();
                default -> throw new IllegalStateException("Неизвестная роль: " + role);
            }
            for (int i = 0; i < listToEdit.size(); i++) {
                if (listToEdit.get(i).getId().equals(recipeId)) {
                    listToEdit.set(i, newRecipe);
                    break;
                }
            }
            dietRepository.save(d);
        }
        if (recipe.isDefault()) {
            recipeRepository.delete(recipe);
        }
        logger.info("Рецепт успешно заменён");
    }

    @Transactional
    public void replaceRecipeInDiet(Long dietId, Long oldRecipeId, String newName) {
        Diet diet = dietRepository.findById(dietId)
                .orElseThrow(() -> new RuntimeException("Диета не найдена: " + dietId));

        Recipe oldRecipe = recipeRepository.findById(oldRecipeId)
                .orElseThrow(() -> new RuntimeException("Рецепт не найден: " + oldRecipeId));

        Recipe newRecipe = recipeRepository.findByName(newName).stream()
                .filter(r -> !r.getId().equals(oldRecipeId))
                .findFirst()
                .orElseGet(() -> {
                    Recipe r = new Recipe();
                    r.setName(newName);
                    r.setDescription("default");
                    r.setTheme("default");
                    r.setDefault(true);
                    return recipeRepository.save(r);
                });

        List<Recipe> listToEdit;
        if (diet.getRecipesForBreakfast().removeIf(r -> r.getId().equals(oldRecipeId))) {
            listToEdit = diet.getRecipesForBreakfast();
        } else if (diet.getRecipesForLunch   ().removeIf(r -> r.getId().equals(oldRecipeId))) {
            listToEdit = diet.getRecipesForLunch();
        } else if (diet.getRecipesForDiner   ().removeIf(r -> r.getId().equals(oldRecipeId))) {
            listToEdit = diet.getRecipesForDiner();
        } else {
            throw new RuntimeException("Рецепт не привязан к этой диете");
        }

        listToEdit.add(newRecipe);
        dietRepository.save(diet);

        if (oldRecipe.isDefault()) {
            recipeRepository.delete(oldRecipe);
        }
    }
}

