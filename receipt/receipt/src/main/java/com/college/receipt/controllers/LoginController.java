package com.college.receipt.controllers;

import com.college.receipt.entities.UserDto;
import com.college.receipt.service.User.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.college.receipt.controllers.RecipeController.logger;

@RestController
public class LoginController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody ){
        userService.registerNewUserAccount()
        logger.info("Открыта страница входа в аккаунт");
        return "login";
    }
}
