package com.college.receipt.repositories;

import com.college.receipt.entities.Recipe;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    @Query(value="SELECT * FROM recipes r WHERE LOWER(r.name) LIKE %:keyword%", nativeQuery = true)
    List<Recipe> findByKeyword(@Param("keyword") String keyword);
    @Query("SELECT r FROM Recipe r WHERE LOWER(r.name) = LOWER(:name)")
    List<Recipe> findByName(@Param("name") String name);
    @Query(value = "SELECT * FROM recipes r " +
            "WHERE (:name IS NULL OR LOWER(r.name) LIKE LOWER(:name)) " +
            "AND (:count_portion IS NULL OR r.count_portion = :count_portion) " +
            "AND (:kkal IS NULL OR r.kkal <= :kkal) " +
            "AND (:time_to_cook IS NULL OR r.time_to_cook <= :time_to_cook) " +
            "AND (:national_kitchen IS NULL OR LOWER(r.national_kitchen) = LOWER(:national_kitchen)) " +
            "AND (:restrictions IS NULL OR LOWER(r.restrictions) = LOWER(:restrictions)) " +
            "AND (:theme IS NULL OR LOWER(r.theme) = LOWER(:theme)) " +
            "AND (:type_of_cook IS NULL OR LOWER(r.type_of_cook) = LOWER(:type_of_cook)) " +
            "AND (:type_of_food IS NULL OR LOWER(r.type_of_food) = LOWER(:type_of_food))",
            nativeQuery = true)
    List<Recipe> findByFilter(@Param("name") String name,
                              @Param("count_portion") Integer countPortion,
                              @Param("kkal") Integer kkal,
                              @Param("time_to_cook") Integer timeToCook,
                              @Param("national_kitchen") String nationalKitchen,
                              @Param("restrictions") String restrictions,
                              @Param("theme") String theme,
                              @Param("type_of_cook") String typeOfCook,
                              @Param("type_of_food") String typeOfFood);
}
