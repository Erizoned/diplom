package com.college.receipt.DTO;

import com.college.receipt.entities.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class RecipeDto {
    private Recipe recipe;
    private UploadedFile photoFood;
    private List<Steps> steps;
    private List<Ingredients> ingredients;
    private String authorUsername;
}
