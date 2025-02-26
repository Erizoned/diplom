package com.college.receipt.controllers;

import com.college.receipt.entities.User;
import com.college.receipt.entities.UserDto;
import com.college.receipt.exceptions.UserAlreadyExistException;
import com.college.receipt.service.User.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.stereotype.Controller;

@RestController
public class RegistrationController {

    @Autowired
    private IUserService userService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserDto());
        return "registration";
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUserAccount(
            @RequestBody @Valid UserDto userDto,
            BindingResult bindingResult) {

        if(bindingResult.hasErrors()){
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        try {
            User registered = userService.registerNewUserAccount(userDto);
            return ResponseEntity.ok(registered);

        } catch (UserAlreadyExistException uaeEx) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Ошибка. Аккаунт уже существует");
        }
        
    }

}
