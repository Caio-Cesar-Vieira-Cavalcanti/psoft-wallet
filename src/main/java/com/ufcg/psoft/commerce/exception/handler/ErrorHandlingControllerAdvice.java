package com.ufcg.psoft.commerce.exception.handler;

import com.ufcg.psoft.commerce.exception.asset.*;
import com.ufcg.psoft.commerce.exception.client.ClientIdNotFoundException;
import com.ufcg.psoft.commerce.exception.user.UnauthorizedUserAccessException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;

@ControllerAdvice
public class ErrorHandlingControllerAdvice {

    private CustomErrorType defaultCustomErrorTypeConstruct(String message) {
        return CustomErrorType.builder()
                .timestamp(LocalDateTime.now())
                .errors(new ArrayList<>())
                .message(message)
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public CustomErrorType onMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        CustomErrorType customErrorType = defaultCustomErrorTypeConstruct(
                "Validation errors found"
        );
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            customErrorType.getErrors().add(fieldError.getDefaultMessage());
        }
        return customErrorType;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public CustomErrorType onConstraintViolation(ConstraintViolationException e) {
        CustomErrorType customErrorType = defaultCustomErrorTypeConstruct(
                "Validation errors found"
        );
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            customErrorType.getErrors().add(violation.getMessage());
        }
        return customErrorType;
    }

    @ExceptionHandler(InvalidQuotationVariationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public CustomErrorType onInvalidQuotationVariationException(InvalidQuotationVariationException e) {
        return defaultCustomErrorTypeConstruct(
                e.getMessage()
        );
    }

    @ExceptionHandler(InvalidAssetTypeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public CustomErrorType onInvalidAssetTypeException(InvalidAssetTypeException e) {
        return defaultCustomErrorTypeConstruct(
                e.getMessage()
        );
    }

    @ExceptionHandler(AssetNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public CustomErrorType onAssetNotFoundException(AssetNotFoundException e) {
        return defaultCustomErrorTypeConstruct(
                e.getMessage()
        );
    }

    @ExceptionHandler(AssetTypeNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public CustomErrorType onAssetTypeNotFoundException(AssetTypeNotFoundException e) {
        return defaultCustomErrorTypeConstruct(
                e.getMessage()
        );
    }

    @ExceptionHandler(UnauthorizedUserAccessException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public CustomErrorType handleUnauthorizedAdminAccessException(UnauthorizedUserAccessException e) {
        return defaultCustomErrorTypeConstruct(
                e.getMessage()
        );
    }

    @ExceptionHandler(ClientIdNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public CustomErrorType onClientIdNotFoundException(ClientIdNotFoundException e) {
        return defaultCustomErrorTypeConstruct(
                e.getMessage()
        );
    }

    @ExceptionHandler(AssetReferencedInPurchaseException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public CustomErrorType onAssetReferencedInPurchaseException(AssetReferencedInPurchaseException e) {
        return defaultCustomErrorTypeConstruct(
                e.getMessage()
        );
    }

}
