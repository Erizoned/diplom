package com.college.receipt.controllers;

import com.college.receipt.entities.User;
import com.college.receipt.DTO.UserDto;
import com.college.receipt.exceptions.UserAlreadyExistException;
import com.college.receipt.service.User.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;


@RestController
public class UserController {

    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody User user) {
        String token = userService.verify(user);
        logger.info("Попытка входа в аккаунт");

        if (token != null) {
            logger.info("Пользователь {} вошёл в аккаунт", user.getEmail());
            return ResponseEntity.ok(Collections.singletonMap("token", token)); // Возвращаем JSON
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Invalid credentials"));
        }
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
