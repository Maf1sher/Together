package org.mafisher.togetherbackend.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum BusinessErrorCodes{

    NO_CODE(0,"No code", HttpStatus.NOT_IMPLEMENTED),
    ACCOUNT_LOCKED(302, "User account locked", HttpStatus.FORBIDDEN),
    ACCOUNT_DISABLED(303, "User account is disable", HttpStatus.FORBIDDEN),
    BAD_CREDENTIALS(304, "Login and / or password is incorrect", HttpStatus.FORBIDDEN),
    EMAIL_IS_USED(305, "Email is used", HttpStatus.BAD_REQUEST),
    NICKNAME_IS_USED(306, "Nick name is used", HttpStatus.BAD_REQUEST),
    USER_IS_ENABLE(307, "User is already activated", HttpStatus.BAD_REQUEST),
    BAD_COOKIE(308, "No jwt cookie found", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(309,"Invalid jwt token", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED(312, "Token expired", HttpStatus.BAD_REQUEST),
    ;
    @Getter
    private final int code;
    @Getter
    private final String description;
    @Getter
    private final HttpStatus httpStatus;
}
