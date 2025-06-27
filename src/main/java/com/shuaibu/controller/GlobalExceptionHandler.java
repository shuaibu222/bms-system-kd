package com.shuaibu.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {

        logger.error("Generic Exception", ex);

        ModelAndView modelAndView = new ModelAndView("errors/error");
        modelAndView.addObject("message", ex.getMessage());
        
        // Get the Referer header to know the previous page
        String referer = request.getHeader("Referer");
        modelAndView.addObject("previousPage", referer != null ? referer : "/dashboard"); // default to dashboard if referer is null
        return modelAndView;
    }
}
