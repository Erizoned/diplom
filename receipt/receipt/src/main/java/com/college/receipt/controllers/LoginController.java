package com.college.receipt.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.college.receipt.controllers.RecipeController.logger;

@RequestMapping("/user")
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(Model model){
        logger.info("Открыта страница входа в аккаунт");
        return "login";
    }
}
