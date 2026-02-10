package com.example.autodepot.controller;

import com.example.autodepot.exception.ExceptionCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Centralized exception handling for Thymeleaf / fleet MVC controllers.
 * Exceptions are converted to redirect with flash message and recorded in the audit log.
 */
@ControllerAdvice(assignableTypes = FleetController.class)
public class FleetControllerAdvice {

    private static final Logger log = LoggerFactory.getLogger(FleetControllerAdvice.class);

    private final ExceptionCollector exceptionCollector;

    public FleetControllerAdvice(ExceptionCollector exceptionCollector) {
        this.exceptionCollector = exceptionCollector;
    }

    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException ex, RedirectAttributes redirectAttributes) {
        exceptionCollector.record(ex, "FleetControllerAdvice.handleRuntimeException");
        log.warn("Fleet action failed: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage() != null ? ex.getMessage() : "An error occurred");
        return "redirect:/fleet/dashboard";
    }
}
