package com.mycompany.vut4.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {
    @ModelAttribute("currentUrl")
    public String addCurrentUrl(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
