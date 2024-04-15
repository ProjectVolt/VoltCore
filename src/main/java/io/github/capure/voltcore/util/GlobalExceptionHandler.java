package io.github.capure.voltcore.util;

import io.github.capure.voltcore.exception.InvalidIdException;
import io.github.capure.voltcore.exception.ProblemNotVisibleException;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, List<String>>> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream().map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage()).collect(Collectors.toList());
        return new ResponseEntity<>(getErrorsMap(errors), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, List<String>>> handleParamValidationErrors(HandlerMethodValidationException ex) {
        List<String> errors = ex.getAllValidationResults()
                .stream().map(err -> err.getMethodParameter().getParameterName() + ": " + err.getResolvableErrors().stream().map(MessageSourceResolvable::getDefaultMessage).collect(Collectors.joining(", "))).collect(Collectors.toList());
        return new ResponseEntity<>(getErrorsMap(errors), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, List<String>>> handleAccessDenied(AccessDeniedException ex) {
        return new ResponseEntity<>(getErrorsMap(List.of("Access denied")), new HttpHeaders(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, List<String>>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return new ResponseEntity<>(getErrorsMap(List.of("Invalid argument type")), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidIdException.class)
    public ResponseEntity<Map<String, List<String>>> handleInvalidId(InvalidIdException ex) {
        return new ResponseEntity<>(getErrorsMap(List.of(ex.getMessage() != null ? ex.getMessage() : "Invalid id")), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ProblemNotVisibleException.class)
    public ResponseEntity<Map<String, List<String>>> handleProblemNotVisible(ProblemNotVisibleException ex) {
        return new ResponseEntity<>(getErrorsMap(List.of(ex.getMessage() != null ? ex.getMessage() : "Problem is not publicly accessible, the visible property is set to false")), new HttpHeaders(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, List<String>>> handleMissingParam(MissingServletRequestParameterException ex) {
        String error = ex.getParameterName() + ": " + ex.getMessage();
        return new ResponseEntity<>(getErrorsMap(List.of(error)), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    private Map<String, List<String>> getErrorsMap(List<String> errors) {
        Map<String, List<String>> errorResponse = new HashMap<>();
        errorResponse.put("errors", errors);
        return errorResponse;
    }

}
