package com.college.receipt.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Diet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany
    @JoinTable(
            name = "diet_breakfast",
            joinColumns = @JoinColumn(name = "diet_id"),
            inverseJoinColumns = @JoinColumn(name = "recipe_id")
    )
    @Size(max = 3, message = "Можно указать не более 3 рецептов для завтрака")
    private List<Recipe> recipesForBreakfast;

    @ManyToMany
    @JoinTable(
            name = "diet_lunch",
            joinColumns = @JoinColumn(name = "diet_id"),
            inverseJoinColumns = @JoinColumn(name = "recipe_id")
    )
    @Size(max = 3, message = "Можно указать не более 3 рецептов для обеда")
    private List<Recipe> recipesForLunch;

    @ManyToMany
    @JoinTable(
            name = "diet_dinner",
            joinColumns = @JoinColumn(name = "diet_id"),
            inverseJoinColumns = @JoinColumn(name = "recipe_id")
    )
    @Size(max = 3, message = "Можно указать не более 3 рецептов для ужина")
    private List<Recipe> recipesForDiner;

    @Column(length = 1000)
    private String recommendation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "diets" })
    private User user;

    private LocalDate term;

    private LocalDate dateOfCreation;
}
