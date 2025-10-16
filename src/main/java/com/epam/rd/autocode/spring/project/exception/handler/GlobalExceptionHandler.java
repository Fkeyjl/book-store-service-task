package com.epam.rd.autocode.spring.project.exception.handler;

import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public String handleEntityNotFoundException(EntityNotFoundException ex, HttpServletRequest request, Model model) {
        log.warn("Entity Not Found: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        model.addAttribute("status", 404);
        model.addAttribute("error", "Not Found");
        model.addAttribute("message", ex.getMessage() != null ? ex.getMessage() : "The requested resource was not found.");
        return "error/error";
    }

    @ExceptionHandler(NotFoundException.class)
    public String handleNotFoundException(NotFoundException ex, HttpServletRequest request, Model model) {
        log.warn("Not Found: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        model.addAttribute("status", 404);
        model.addAttribute("error", "Not Found");
        model.addAttribute("message", ex.getMessage());
        return "error/error";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request, Model model) {
        log.warn("Access Denied: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        model.addAttribute("status", 403);
        model.addAttribute("error", "Access Denied");
        model.addAttribute("message", "You don't have permission to access this resource.");
        return "error/error";
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        log.warn("Validation failed: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(org.springframework.web.servlet.resource.NoResourceFoundException ex,
                                                                     HttpHeaders headers,
                                                                     HttpStatusCode status,
                                                                     WebRequest request) {
        log.warn("No Resource Found: {}", ex.getMessage());
        throw new NotFoundException("The requested page or resource does not exist.");
    }

    @ExceptionHandler(AlreadyExistException.class)
    public String handleAlreadyExistException(AlreadyExistException ex,
                                              HttpServletRequest request,
                                              RedirectAttributes redirectAttributes) {
        log.warn("Resource already exists: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        String requestPath = request.getRequestURI();
        redirectAttributes.addFlashAttribute("creatingError", ex.getMessage());
        return "redirect:" + requestPath;
    }

    @ExceptionHandler(Exception.class)
    public String handleAllExceptions(Exception ex, HttpServletRequest request, Model model) {
        log.error("Unexpected error occurred at {}: ", request.getRequestURI(), ex);
        model.addAttribute("status", 500);
        model.addAttribute("error", "Internal Server Error");
        model.addAttribute("message", "An unexpected server error occurred. Please try again later.");
        return "error/error";
    }

}
