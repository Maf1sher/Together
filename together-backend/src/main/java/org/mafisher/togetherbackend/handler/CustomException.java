package org.mafisher.togetherbackend.handler;

import lombok.Getter;


@Getter
public final class CustomException extends RuntimeException{
    BusinessErrorCodes errorCode;

    public CustomException(BusinessErrorCodes businessErrorCodes) {
        super(businessErrorCodes.getDescription());
        this.errorCode = businessErrorCodes;
    }
}
