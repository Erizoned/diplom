package com.college.receipt.DTO;

import com.college.receipt.entities.Diet;
import com.college.receipt.entities.Rating;
import com.college.receipt.entities.Recipe;
import com.college.receipt.validation.PasswordMatches;
import com.college.receipt.validation.ValidEmail;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@PasswordMatches
public class UserDto {
    @NotNull
    @NotEmpty
    private String username;

    @NotNull
    @NotEmpty
    private String password;
    private String matchingPassword;

    @NotNull
    @NotEmpty
    @ValidEmail
    private String email;

    private List<Recipe> recipes;

    private List<Rating> ratings;

    private List<Diet> diets;

}