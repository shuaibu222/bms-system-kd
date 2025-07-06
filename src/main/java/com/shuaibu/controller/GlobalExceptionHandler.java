package com.shuaibu.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        logError("IllegalArgumentException", ex, request);
        return buildErrorView(ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleAllExceptions(Exception ex, HttpServletRequest request) {
        logError("Unhandled Exception", ex, request);
        return buildErrorView("An unexpected error occurred.", request);
    }

    private ModelAndView buildErrorView(String message, HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("errors/error");
        modelAndView.addObject("message", message != null ? message : "Something went wrong.");

        String referer = request.getHeader("Referer");
        modelAndView.addObject("previousPage", referer != null ? referer : "/dashboard");
        return modelAndView;
    }

    private void logError(String type, Exception ex, HttpServletRequest request) {
        logger.error("{} at URI: {} | User-Agent: {}",
                type,
                request.getRequestURI(),
                request.getHeader("User-Agent"),
                ex);
    }
}
