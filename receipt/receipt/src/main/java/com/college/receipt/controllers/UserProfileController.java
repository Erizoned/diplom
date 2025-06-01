package com.college.receipt.controllers;

import com.college.receipt.DTO.SimplifiedUserDto;
import com.college.receipt.entities.Rating;
import com.college.receipt.entities.Recipe;
import com.college.receipt.entities.User;
import com.college.receipt.repositories.RatingRepository;
import com.college.receipt.repositories.RecipeRepository;
import com.college.receipt.repositories.UserRepository;
import com.college.receipt.service.User.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api")
@RestController
public class UserProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @GetMapping("/user/{id}")
    private ResponseEntity<User> getUser(@PathVariable("id") Long id){
        User user = userService.findAuthenticatedUser();
        List<Recipe> recipe = recipeRepository.findByUser(user);
        List<Rating> rating = ratingRepository.findByUserAndRecipe()
        SimplifiedUserDto response = new SimplifiedUserDto(
                user.getUsername(),
                user.getEmail(),
                recipe,

        );
        return ResponseEntity.ok().body(user);
    }
}
