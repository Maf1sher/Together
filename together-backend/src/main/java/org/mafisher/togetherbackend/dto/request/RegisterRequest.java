package org.mafisher.togetherbackend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterRequest {
    @NotNull(message = "Firstname is required")
    private String firstName;
    @NotNull(message = "Lastname is required")
    private String lastName;
    @NotNull(message = "NickName is required")
    private String nickName;
    @NotNull(message = "Email is required")
    @Email(message = "Email is not formated")
    private String email;
    @NotNull(message = "Password is required")
    @Size(min = 8, message = "Password should be 8 characters long min")
    private String password;
}
