//package com.college.receipt.controllers;
//
//
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ModelAttribute;
//
//@ControllerAdvice
//public class GlobalControllerAdvice {
//
//    @ModelAttribute("user")
//    public String currentUser() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
//            return authentication.getName(); // Возвращаем имя текущего пользователя
//        }
//        return null; // Если пользователь не авторизован
//    }
//}
