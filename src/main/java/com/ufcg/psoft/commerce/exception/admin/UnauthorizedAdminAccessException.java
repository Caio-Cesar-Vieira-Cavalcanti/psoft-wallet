package com.ufcg.psoft.commerce.exception.admin;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedAdminAccessException extends RuntimeException {
    public UnauthorizedAdminAccessException() {
      super("Unauthorized admin access");
    }
}
