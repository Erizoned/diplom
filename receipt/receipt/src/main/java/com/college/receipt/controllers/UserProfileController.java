package com.college.receipt.controllers;

import com.college.receipt.DTO.DietDto;
import com.college.receipt.DTO.SimplifiedRecipeDto;
import com.college.receipt.DTO.SimplifiedUserDto;
import com.college.receipt.entities.*;
import com.college.receipt.repositories.DietRepository;
import com.college.receipt.repositories.RatingRepository;
import com.college.receipt.repositories.RecipeRepository;
import com.college.receipt.repositories.UserRepository;
import com.college.receipt.service.User.UserService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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

    @Autowired
    private DietRepository dietRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);

    @GetMapping("/user/profile")
    private ResponseEntity<SimplifiedUserDto> getUser(){
        User user = userService.findAuthenticatedUser();
        List<Recipe> recipes = recipeRepository.findByCreatedBy(user);
        List<SimplifiedRecipeDto> recipeSummaries = recipes.stream()
                .map(r -> {
                    String avatar = r.getPhotos().stream()
                            .filter(UploadedFile::isPhotoFood)
                            .map(UploadedFile::getFilePath)
                            .findFirst()
                            .orElse(null);

                    return new SimplifiedRecipeDto(
                            r.getId(),
                            r.getName(),
                            r.getDescription(),
                            avatar
                    );
                })
                .collect(Collectors.toList());
        List<Rating> ratings = ratingRepository.findByUser(user);
        List<Diet> diets = dietRepository.findByUser(user);
        List<DietDto> dietDtos = diets.stream()
                .map(d -> {
                    return new DietDto(
                    d.getId(),
                    d.getRecommendation(),
                    d.getTerm(),
                    d.getDateOfCreation()
                    );
                }
                ).toList();
        SimplifiedUserDto response = new SimplifiedUserDto(
                user.getUsername(),
                user.getEmail(),
                recipeSummaries,
                ratings,
                dietDtos
        );
        logger.info("Запрос на получение пользователя {}", user.getUsername());
        return ResponseEntity.ok().body(response);
    }
}
