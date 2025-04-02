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
    ;
    @Getter
    private final int code;
    @Getter
    private final String description;
    @Getter
    private final HttpStatus httpStatus;
}
