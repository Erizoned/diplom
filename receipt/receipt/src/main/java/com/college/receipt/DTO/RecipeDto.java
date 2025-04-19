package com.college.receipt.DTO;

import com.college.receipt.entities.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeDto {
    private Recipe recipe;
    private UploadedFile photoFood;
    private List<Steps> steps;
    private List<Ingredients> ingredients;
    private String authorUsername;
    private List<CommentDto> comments;

    @Override
    public String toString() {
        return "RecipeDto{" +
                "ingredients=" + ingredients +
                ", recipe=" + recipe +
                '}';
    }

}
