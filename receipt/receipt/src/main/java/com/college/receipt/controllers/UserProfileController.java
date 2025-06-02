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
    private UserService userService;

    @GetMapping("/user/profile")
    private ResponseEntity<SimplifiedUserDto> getUser(){
        SimplifiedUserDto response = userService.getSimplifiedUser();
        return ResponseEntity.ok().body(response);
    }
}
