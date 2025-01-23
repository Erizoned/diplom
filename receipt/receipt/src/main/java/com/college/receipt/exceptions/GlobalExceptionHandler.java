package com.college.receipt.exceptions;


import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(recipesNotFoundException.class)
    public String handleRecipecNotFoundException(recipesNotFoundException ex, Model model){
        model.addAttribute("errorMessage", ex.getMessage());
        return "index";
    }
}
