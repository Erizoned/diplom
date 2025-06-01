package com.college.receipt.DTO;

import com.college.receipt.entities.Diet;
import com.college.receipt.entities.Rating;
import com.college.receipt.entities.Recipe;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimplifiedUserDto {

    private String username;

    private String email;

    private List<Recipe> recipes;

    private List<Rating> ratings;

    private List<Diet> diets;
}
