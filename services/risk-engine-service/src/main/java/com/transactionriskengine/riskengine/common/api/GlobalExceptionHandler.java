package com.transactionriskengine.riskengine.common.api;

import com.transactionriskengine.riskengine.common.exception.RiskDecisionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RiskDecisionNotFoundException.class)
    public ResponseEntity<ApiError> handleRiskDecisionNotFound(
            RiskDecisionNotFoundException exception
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException exception
    ) {
        String message = exception.getRequiredType() != null
                && exception.getRequiredType().isEnum()
                ? exception.getName() + " has an invalid value: " + exception.getValue()
                : exception.getName() + " has an invalid value";

        return ResponseEntity.badRequest()
                .body(ApiError.of(message));
    }
}
