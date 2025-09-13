package com.example.authService.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1003, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1004, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1005, "You do not have permission", HttpStatus.FORBIDDEN),
    WRONG_PASSWORD(1006, " Password not correct", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(1007, "Role not founded", HttpStatus.NOT_FOUND),
    REFRESH_TOKEN_INVALID(1007, "Refresh token invalid or expired", HttpStatus.UNAUTHORIZED),
    INVALIDATED_TOKEN(1008, "The token has been logged out. ", HttpStatus.UNAUTHORIZED),
    PASSWORD_NOT_SAME(1009, "New password and repeat password do not match", HttpStatus.BAD_REQUEST),
    NEW_PASSWORD_SAME_AS_OLD(1010, "New password and old password do not same", HttpStatus.BAD_REQUEST),
    CANNOT_SEND_EMAIL(1011, "Cannot send email", HttpStatus.BAD_REQUEST),
    OTP_INVALID(1012, "OTP code is invalid or expired.", HttpStatus.BAD_REQUEST);

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
