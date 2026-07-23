package com.santander.msauthservices.controller.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.santander.msauthservices.constants.UserConstants;
import com.santander.msauthservices.exception.AlreadyExistsException;
import com.santander.msauthservices.exception.BadRequestException;
import com.santander.msauthservices.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handlerUserIllegalArgumentException(IllegalArgumentException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        Problem problem = getProblem(status.value(), status.name(), ex.getMessage(), null);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handlerUserNotFoundArgumentException(NotFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        Problem problem = getProblem(status.value(), status.name(), ex.getMessage(), null);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handlerUserBadRequestException(BadRequestException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        Problem problem = getProblem(status.value(), status.name(), ex.getMessage(), null);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<Object> handlerUserAlreadyActivationException(AlreadyExistsException ex) {
        HttpStatus status = HttpStatus.CONFLICT;

        Problem problem = getProblem(status.value(), status.name(), ex.getMessage(), null);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<Field> camposDeErros = new ArrayList<>();

        for (ObjectError error : ex.getBindingResult().getAllErrors()) {
            String name = (error instanceof FieldError fieldError)
                    ? fieldError.getField()
                    : error.getObjectName();
            String message = messageSource.getMessage(error, LocaleContextHolder.getLocale());

            camposDeErros.add(new Field(name, message));
        }

        Problem problem = getProblem(
                status.value(),
                HttpStatus.valueOf(status.value()).name(),
                UserConstants.ARGUMENT_INVALID,
                camposDeErros);

        return super.handleExceptionInternal(ex, problem, headers, status, request);
    }

    private Problem getProblem(Integer status, String error, String message, List<Field> fields) {
        return new Problem(status, OffsetDateTime.now(), error, message, fields);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Problem(Integer status, OffsetDateTime timestamp, String error, String message, List<Field> fields) {
    }

    public record Field(String name, String message) {
    }
}