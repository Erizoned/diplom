package com.college.receipt.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

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
    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Column(length = 500)
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
    private Integer countPortion;

    @Column(name = "national_kitchen")
    private String nationalKitchen;

    @Column(name = "kkal")
    private Integer kkal;

    @Column(name = "time_to_cook")
    private Integer timeToCook;

    @Column
    private boolean isDefault;

    @Column
    private Double avgRating;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ingredients> ingredients = new ArrayList<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    private List<UploadedFile> photos;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Steps> steps = new ArrayList<>();

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "user_id")
    private User createdBy;

    @JsonIgnore
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comments> comments;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rating> ratings;

    public boolean hasOnlyName() {
        return name != null && !name.isBlank()
                && (description == null || description.isBlank())
                && (theme == null || theme.isBlank())
                && (typeOfFood == null || typeOfFood.isBlank())
                && (typeOfCook == null || typeOfCook.isBlank())
                && (restrictions == null || restrictions.isBlank())
                && countPortion == null
                && nationalKitchen == null
                && kkal == null
                && timeToCook == null
                && (ingredients == null || ingredients.isEmpty());
    }

    public boolean hasIngredients(){
        return !this.getIngredients().isEmpty();
    }

    @Override
    public String toString() {
        return "Recipe{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", theme='" + theme + '\'' +
                ", typeOfFood='" + typeOfFood + '\'' +
                ", timeToCook=" + timeToCook +
                ", nationalKitchen='" + nationalKitchen + '\'' +
                ", restrictions='" + restrictions + '\'' +
                ", typeOfCook='" + typeOfCook + '\'' +
                ", kkal=" + kkal +
                ", countPortion=" + countPortion +
                '}';
    }

}
