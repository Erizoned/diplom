package com.college.receipt.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import com.college.receipt.entities.Steps;

import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "type_of_food")
    private String typeOfFood;

    @Column(name = "type_of_cook")
    private String typeOfCook;

    @Column(name = "restrictions")
    private String restrictions;

    @Column(name = "count_portion")
    private int countPortion;

    @Column(name = "national_kitchen")
    private String nationalKitchen;

    @Column(name = "kkal")
    private int kkal;

    @Column(name = "time_to_cook")
    private int timeToCook;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    private List<UploadedFile> photos;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Steps> steps = new ArrayList<>();


    @Override
    public String toString() {
        return "Recipe{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", theme='" + theme + '\'' +
                ", typeOfFood='" + typeOfFood + '\'' +
                ", timeToCook=" + timeToCook +
                '}';
    }
}
