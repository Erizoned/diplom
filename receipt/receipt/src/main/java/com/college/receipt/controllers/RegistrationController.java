package com.college.receipt.controllers;

import com.college.receipt.entities.User;
import com.college.receipt.entities.UserDto;
import com.college.receipt.exceptions.UserAlreadyExistException;
import com.college.receipt.service.User.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.stereotype.Controller;

@Controller
public class RegistrationController {

    @Autowired
    private IUserService userService;

    @GetMapping("/user/registration")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserDto());
        return "registration";
    }

    @PostMapping("/user/registration")
    public ModelAndView registerUserAccount(
            @ModelAttribute("user") @Valid UserDto userDto,
            HttpServletRequest request,
            Errors errors) {
        ModelAndView mav = new ModelAndView("registration");
        if (errors.hasErrors()) {
            System.out.println("Validation errors: " + errors.getAllErrors());
            mav.addObject("message", "Please correct the errors in the form.");
            return mav;
        }

        try {
            User registered = userService.registerNewUserAccount(userDto);
            mav.setViewName("successRegister");
            mav.addObject("user", registered);
        } catch (UserAlreadyExistException uaeEx) {
            System.out.println("User already exists: " + uaeEx.getMessage());
            mav.addObject("message", "An account for that username/email already exists.");
            return mav;
        }

        return mav;
    }

}
