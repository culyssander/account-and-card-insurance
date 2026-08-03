package com.santander.msclaimsquestionnaireservices.controller.exception;

import com.santander.msclaimsquestionnaireservices.exception.BusinessException;
import com.santander.msclaimsquestionnaireservices.exception.InvalidOptionException;
import com.santander.msclaimsquestionnaireservices.exception.QuestionMismatchException;
import com.santander.msclaimsquestionnaireservices.exception.QuestionNotFoundException;
import com.santander.msclaimsquestionnaireservices.exception.QuestionnaireSessionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class QuestionnaireExceptionHandler {

    @ExceptionHandler({QuestionNotFoundException.class, QuestionnaireSessionNotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleNotFound(RuntimeException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({InvalidOptionException.class, QuestionMismatchException.class})
    public ResponseEntity<Map<String, Object>> handleUnprocessable(RuntimeException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleExpectedError(BusinessException ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "message", message
        ));
    }
}
