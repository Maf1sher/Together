package org.mafisher.togetherbackend.service;

import jakarta.mail.MessagingException;
import org.mafisher.togetherbackend.dto.UserDto;
import org.mafisher.togetherbackend.dto.request.LoginRequest;
import org.mafisher.togetherbackend.dto.request.RegisterRequest;

import java.util.Map;

public interface AuthService {
    UserDto createUser(RegisterRequest registerRequest) throws MessagingException;
    String verify(LoginRequest loginRequest) throws MessagingException;
    void activateUser(Long id, String token) throws MessagingException;
    UserDto verifyToken(String token);
    UserDto findUserByEmail(String email);
    UserDto findUserById(Long id);
    UserDto findUserByNickname(String nickname);
}
