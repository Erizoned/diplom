package com.college.receipt;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "recipes")
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @NotBlank(message = "Описание рецепта не может быть пустым")
    private String description;

    @NotBlank(message = "Блюдо не может быть без темы")
    private String theme;

    @Column(name = "photo_food")
    @Lob
    private byte[] photoFood; // Фото блюда

    @Column(name = "step_photo", columnDefinition = "bytea")
    @Lob
    private byte[] stepPhoto; // Фото шагов приготовления

    @Column(name = "type_of_food")
    private String typeOfFood;

    @Column(name = "type_of_cook")
    private String typeOfCook;

    @Column(name = "restrictions")
    private String restrictions;

    @Column(name = "count_portion")
    private int countPortion;

    @Column(name = "kkal")
    private int kkal;

    @Column(name = "time_to_cook")
    private int timeToCook;
}
