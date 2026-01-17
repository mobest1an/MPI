package com.iverpa.mpi.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLTransientConnectionException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class DatabaseConnectionExceptionHandler {

    @ExceptionHandler(SQLTransientConnectionException.class)
    public ResponseEntity<Map<String, Object>> handleSQLTransientConnectionException(
            SQLTransientConnectionException ex,
            HttpServletRequest request) {

        return buildServiceUnavailableResponse(ex, request, "Database connection failed");
    }


    /**
     * Построение стандартного ответа 503 Service Unavailable
     */
    private ResponseEntity<Map<String, Object>> buildServiceUnavailableResponse(
            Throwable ex,
            HttpServletRequest request,
            String message) {

        Map<String, Object> body = new HashMap<>();

        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        body.put("error", HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase());
        body.put("message", message);
        body.put("path", request.getRequestURI());
        body.put("method", request.getMethod());

        // Добавляем заголовки для retry
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("Retry-After", "30") // Предлагаем повторить через 30 секунд
                .header("X-Service-Status", "temporarily-unavailable")
                .body(body);
    }
}
