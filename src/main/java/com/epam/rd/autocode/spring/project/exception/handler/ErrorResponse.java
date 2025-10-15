package com.epam.rd.autocode.spring.project.exception.handler;

import java.time.LocalDateTime;

public record ErrorResponse(LocalDateTime timestamp,
                            int status,
                            String error,
                            String message) {}
