package com.college.receipt.controllers;

import com.college.receipt.entities.User;
import com.college.receipt.entities.UserDto;
import com.college.receipt.exceptions.UserAlreadyExistException;
import com.college.receipt.service.IUserService;
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

    // Обработка GET-запроса на страницу регистрации
    @GetMapping("/user/registration")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserDto()); // Передаём пустой объект для формы
        return "registration"; // Имя HTML-шаблона регистрации (например, registration.html)
    }

    // Обработка POST-запроса на регистрацию
    @PostMapping("/user/registration")
    public ModelAndView registerUserAccount(
            @ModelAttribute("user") @Valid UserDto userDto,
            HttpServletRequest request,
            Errors errors) {

        ModelAndView mav = new ModelAndView("registration");

        if (errors.hasErrors()) {
            mav.addObject("message", "Please correct the errors in the form.");
            return mav;
        }

        try {
            User registered = userService.registerNewUserAccount(userDto);
            mav.setViewName("successRegister"); // Переход на страницу успешной регистрации
            mav.addObject("user", registered);
        } catch (UserAlreadyExistException uaeEx) {
            mav.addObject("message", "An account for that username/email already exists.");
            return mav;
        }

        return mav;
    }
}
