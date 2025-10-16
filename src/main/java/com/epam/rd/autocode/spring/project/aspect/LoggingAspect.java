package com.epam.rd.autocode.spring.project.aspect;

import com.epam.rd.autocode.spring.project.dto.LoginRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("within(com.epam.rd.autocode.spring.project.controller..*)")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        try {
            Object result = joinPoint.proceed();
            if (isImportantAction(method)) {
                String user = getUserInfo(joinPoint.getArgs());
                log.info("[{}] {}{}", className, getActionName(method),
                        user != null ? " - User: " + user : "");
            }
            return result;
        } catch (Throwable ex) {
            log.error("CONTROLLER_ERROR: [{}.{}] Failed. Message: {}",
                    className,
                    method,
                    ex.getMessage(),
                    ex);
            throw ex;
        }
    }

    @Around("within(com.epam.rd.autocode.spring.project.service..*)")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        Class<?> targetClass = joinPoint.getTarget().getClass();
        String className = targetClass.getSimpleName();
        String method = joinPoint.getSignature().getName();
        log.debug("[{}] Calling: {}", joinPoint.getTarget().getClass().getSimpleName(), method);
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            log.debug("[{}] Completed: {} ({} ms)",
                    joinPoint.getTarget().getClass().getSimpleName(), method, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable ex) {
            log.error("SERVICE_ERROR: [{}.{}] Failed. Message: {}",
                    className,
                    method,
                    ex.getMessage(),
                    ex);
            throw ex;
        }

    }

    @Around("within(com.epam.rd.autocode.spring.project.repo..*)")
    public Object logRepository(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        long startTime = System.currentTimeMillis();
        try {
            log.debug("[{}.{}] Starting data operation.", className, method);

            Object result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - startTime;

            if (executionTime > 100) {
                log.warn("PERFORMANCE WARNING: [{}.{}] Data operation took {}ms.",
                        className, method, executionTime);
            } else {
                log.debug("[{}.{}] Completed in {}ms.", className, method, executionTime);
            }
            return result;
        } catch (Throwable ex) {
            log.error("REPOSITORY_ERROR: [{}.{}] Failed. Message: {}",
                    className,
                    method,
                    ex.getMessage(),
                    ex);
            throw ex;
        }

    }

    private boolean isImportantAction(String method) {
        return method.toLowerCase().matches(".*(login|register|logout|checkout|create|update|delete).*");
    }

    private String getActionName(String method) {
        String lower = method.toLowerCase();
        if (lower.contains("login")) return "Login successful";
        if (lower.contains("register")) return "Registration successful";
        if (lower.contains("logout")) return "Logout successful";
        if (lower.contains("checkout")) return "Checkout completed";
        if (lower.contains("create")) return "Created";
        if (lower.contains("update")) return "Updated";
        if (lower.contains("delete")) return "Deleted";
        return method;
    }

    private String getUserInfo(Object[] args) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return auth.getName();
            }
        } catch (Exception ignored) {
        }

        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof LoginRequest) {
                    return ((LoginRequest) arg).getUsername();
                }
            }
        }

        return null;
    }
}
