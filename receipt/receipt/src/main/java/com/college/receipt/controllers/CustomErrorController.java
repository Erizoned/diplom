package com.college.receipt.controllers;
import jakarta.servlet.RequestDispatcher;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
@Controller
public class CustomErrorController implements ErrorController{

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model){
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String errorMessage = "Произошла ошибка.";

        if(status != null){
            int statuseCode = Integer.parseInt(status.toString());

            switch (statuseCode){
                case 403:
                    errorMessage = "Доступ запрещён!";
                    break;
                case 404:
                    errorMessage = "Страница не найдена!";
                    break;
                case 500:
                    errorMessage = "Внутреняя ошибка сервера!";
                    break;
                default:
                    errorMessage = "Неизвестная ошибка.";
                    break;
            }
            model.addAttribute("statusCode", statuseCode);
            model.addAttribute("message", errorMessage);
        }
        return "error";
    }
}
