package org.mafisher.togetherbackend.email;

import lombok.Getter;

@Getter
public enum EmailTemplateName {
    ACTIVATE_ACCOUNT("activate_account"),
    DELETE_ACCOUNT("delete_account"),
    RESET_PASSWORD("reset_password"),
    ;

    private final String name;

    EmailTemplateName(String name) {
        this.name = name;
    }
}
