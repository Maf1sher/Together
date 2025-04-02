package org.mafisher.togetherbackend.service;

import org.mafisher.togetherbackend.handler.CustomException;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String generateToken(String username);
    String extractUserName(String token);
    boolean validateToken(String token, UserDetails userDetails);
    boolean validateJwtToken(String authToken) throws CustomException;
}
