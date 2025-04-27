package com.college.receipt.service;

import com.college.receipt.entities.Diet;
import com.college.receipt.entities.Recipe;
import com.college.receipt.entities.User;
import com.college.receipt.repositories.DietRepository;
import com.college.receipt.service.User.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DietService {

    @Autowired
    private UserService userService;

    @Autowired
    private DietRepository dietRepository;

    public Diet createDiet(List<Recipe> recipeList){
        User user = userService.findAuthenticatedUser();
        Diet diet = Diet.builder()
                .recipes(recipeList)
                .user(user)
                .term(LocalDate.now().plusDays(30))
                .dateOfCreation(LocalDate.now())
                .build();

        return dietRepository.save(diet);
    }
}
