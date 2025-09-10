package com.ufcg.psoft.commerce.exception.handler;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.ufcg.psoft.commerce.exception.asset.*;
import com.ufcg.psoft.commerce.exception.notification.AlreadySubscribedException;
import com.ufcg.psoft.commerce.exception.purchase.PurchaseNotFoundException;
import com.ufcg.psoft.commerce.exception.user.ClientBudgetIsInsufficientException;
import com.ufcg.psoft.commerce.exception.user.ClientIdNotFoundException;
import com.ufcg.psoft.commerce.exception.user.ClientIsNotPremiumException;
import com.ufcg.psoft.commerce.exception.user.UnauthorizedUserAccessException;
import com.ufcg.psoft.commerce.exception.withdraw.WithdrawNotFoundException;
import com.ufcg.psoft.commerce.exception.user.ClientHoldingIsInsufficientException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
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
    public CustomErrorType handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
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
    public CustomErrorType handleConstraintViolation(ConstraintViolationException e) {
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
    public CustomErrorType handleInvalidQuotationVariationException(InvalidQuotationVariationException e) {
        return defaultCustomErrorTypeConstruct(
                e.getMessage()
        );
    }

    @ExceptionHandler(AssetIsAlreadyActive.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public CustomErrorType handleAssetIsAlreadyActive(AssetIsAlreadyActive e) {
        return defaultCustomErrorTypeConstruct(
                e.getMessage()
        );
    }

    @ExceptionHandler(AssetIsInactiveException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public CustomErrorType handleAssetIsInactive(AssetIsInactiveException e) {
        return defaultCustomErrorTypeConstruct(
                e.getMessage()
        );
    }

    @ExceptionHandler(AssetIsNotStockNeitherCryptoException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public CustomErrorType handleAssetIsNotStockNeitherCrypto(AssetIsNotStockNeitherCryptoException e) {
        return defaultCustomErrorTypeConstruct(
                e.getMessage()
        );
    }

    @ExceptionHandler(InvalidAssetTypeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public CustomErrorType handleInvalidAssetTypeException(InvalidAssetTypeException e) {
        return defaultCustomErrorTypeConstruct(
                e.getMessage()
        );
    }

    @ExceptionHandler(AssetNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public CustomErrorType handleAssetNotFoundException(AssetNotFoundException e) {
        return defaultCustomErrorTypeConstruct(
                e.getMessage()
        );
    }

    @ExceptionHandler(AssetTypeNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public CustomErrorType handleAssetTypeNotFoundException(AssetTypeNotFoundException e) {
        return defaultCustomErrorTypeConstruct(
                e.getMessage()
        );
    }

    @ExceptionHandler(AssetQuantityAvailableIsInsufficientException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public CustomErrorType handleAssetQuantityAvailableIsInsufficient(AssetQuantityAvailableIsInsufficientException e) {
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

    @ExceptionHandler(ClientIsNotPremiumException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public CustomErrorType handleClientIsNotPremium(ClientIsNotPremiumException e) {
        return defaultCustomErrorTypeConstruct(
                e.getMessage()
        );
    }

    @ExceptionHandler(ClientIdNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public CustomErrorType handleClientIdNotFoundException(ClientIdNotFoundException e) {
        return defaultCustomErrorTypeConstruct(
                e.getMessage()
        );
    }

    @ExceptionHandler(ClientBudgetIsInsufficientException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public CustomErrorType handleClientBudgetIsInsufficientException(ClientBudgetIsInsufficientException e) {
        return defaultCustomErrorTypeConstruct(
                e.getMessage()
        );
    }

    @ExceptionHandler(AssetReferencedInPurchaseException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public CustomErrorType handleAssetReferencedInPurchaseException(AssetReferencedInPurchaseException e) {
        return defaultCustomErrorTypeConstruct(
                e.getMessage()
        );
    }

    @ExceptionHandler(AlreadySubscribedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public CustomErrorType handleAlreadySubscribedException(AlreadySubscribedException e) {
        return defaultCustomErrorTypeConstruct(
                e.getMessage()
        );
    }

    @ExceptionHandler(PurchaseNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public CustomErrorType handlePurchaseNotFoundException(PurchaseNotFoundException e) {
        return defaultCustomErrorTypeConstruct(
                e.getMessage()
        );
    }

    @ExceptionHandler(WithdrawNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public CustomErrorType handleWithdrawNotFoundException(WithdrawNotFoundException e) {
        return defaultCustomErrorTypeConstruct(
                e.getMessage()
        );
    }

    @ExceptionHandler(ClientHoldingIsInsufficientException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public CustomErrorType handleClientHoldingIsInsufficientException(ClientHoldingIsInsufficientException e) {
        return defaultCustomErrorTypeConstruct(
                e.getMessage()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public CustomErrorType handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();

        while (cause != null) {
            if (cause instanceof InvalidFormatException invalidFormatException && invalidFormatException.getTargetType() == java.util.UUID.class) {
                    return defaultCustomErrorTypeConstruct(
                            "Invalid UUID format: please provide a UUID with 36 characters in the format xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx."
                    );
            }
            cause = cause.getCause();
        }

        return defaultCustomErrorTypeConstruct(
                "Malformed JSON request."
        );
    }
}
