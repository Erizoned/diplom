package com.college.receipt.controllers;

import com.college.receipt.entities.User;
import com.college.receipt.entities.UserDto;
import com.college.receipt.exceptions.UserAlreadyExistException;
import com.college.receipt.service.User.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;


@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public String login(@RequestBody User user) throws AuthenticationException {
        return userService.verify(user);
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
