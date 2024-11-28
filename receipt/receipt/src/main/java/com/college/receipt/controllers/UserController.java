package com.college.receipt.controllers;

import com.college.receipt.entities.User;
import com.college.receipt.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
@RequestMapping("/users")
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/hello")
    public ResponseEntity<String> greet() {
        return ResponseEntity.ok("Hello world");
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<User> findById(@PathVariable("id") Long id) {
        return userService.findUserById(id)
                .map(user -> ResponseEntity.ok(user))
                .orElseThrow(() -> new UserNotFoundException("Пользователь с айди " + id + " не найден"));
    }

    public class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    public class InvalidUserDataException extends RuntimeException {
        public InvalidUserDataException(String message) {
            super(message);
        }
    }

    @DeleteMapping("/user/delete/{id}")
    public ResponseEntity<User> deleteById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(userService.deleteUserById(id));
    }

    @PostMapping("/user")
    public ResponseEntity<User> registerUser(@Valid @RequestBody User newUser) {
        User savedUser = userService.registerUser(newUser);
        if (savedUser.getEmail() == null || savedUser.getUsername() == null){
            throw new InvalidUserDataException("Ошибка валидации");
        }
        return ResponseEntity.ok(savedUser);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        return new ResponseEntity<>("Ошибка валидации: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @GetMapping
    public ResponseEntity<List<User>> getUsers(){
        return ResponseEntity.ok(userService.findAllUsers());
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable("id") Long id, @RequestBody User user){
        return ResponseEntity.ok(userService.updateUser(user));
    }

    @PostMapping("/{username}/roles")
    public ResponseEntity<?> assignRoleToUser(@PathVariable String username, @RequestParam String roleName) {
        userService.assignRoleToUser(username, roleName);
        return ResponseEntity.ok("Role " + roleName + " assigned to user " + username);
    }

}
