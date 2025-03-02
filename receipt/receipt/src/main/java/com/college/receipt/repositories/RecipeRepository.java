package com.college.receipt.repositories;

import com.college.receipt.entities.Recipe;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    Optional<Recipe> findRecipeById(Long id);
    @Query(value="SELECT * FROM recipes r WHERE r.name LIKE %:keyword%", nativeQuery = true)
    List<Recipe> findByKeyword(@Param("keyword") String keyword);
    @Query(value = "SELECT * FROM recipes r " +
            "WHERE (:count_portion IS NULL OR r.count_portion = :count_portion) " +
            "AND (:kkal IS NULL OR r.kkal <= :kkal) " +
            "AND (:time_to_cook IS NULL OR r.time_to_cook <= :time_to_cook) " +
            "AND (:national_kitchen IS NULL OR r.national_kitchen = :national_kitchen) " +
            "AND (:restrictions IS NULL OR r.restrictions = :restrictions) " +
            "AND (:theme IS NULL OR r.theme = :theme) " +
            "AND (:type_of_cook IS NULL OR r.type_of_cook = :type_of_cook) " +
            "AND (:type_of_food IS NULL OR r.type_of_food = :type_of_food)",
            nativeQuery = true)
    List<Recipe> findByFilter(@Param("count_portion") Integer countPortion,
                              @Param("kkal") Integer kkal,
                              @Param("time_to_cook") Integer timeToCook,
                              @Param("national_kitchen") String nationalKitchen,
                              @Param("restrictions") String restrictions,
                              @Param("theme") String theme,
                              @Param("type_of_cook") String typeOfCook,
                              @Param("type_of_food") String typeOfFood);


}
