package org.mafisher.togetherbackend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginRequest {
    @NotNull(message = "Email is required")
    @Email(message = "Email is not formated")
    private String email;
    @NotNull(message = "Password is required")
    private String password;
}
