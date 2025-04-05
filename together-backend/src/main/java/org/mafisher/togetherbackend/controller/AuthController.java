package org.mafisher.togetherbackend.controller;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mafisher.togetherbackend.dto.UserDto;
import org.mafisher.togetherbackend.dto.request.LoginRequest;
import org.mafisher.togetherbackend.dto.request.RegisterRequest;
import org.mafisher.togetherbackend.service.AuthService;
import org.mafisher.togetherbackend.service.CookieService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieService cookieService;

    @Value("${mailing.frontend.redirect-url}")
    private String redirectUrl;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest registerRequest) throws MessagingException {
        UserDto createdUser = authService.createUser(registerRequest);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest, HttpServletResponse response)
            throws MessagingException {
        String token = authService.verify(loginRequest);
        response.addCookie(cookieService.getNewCookie("jwt", token));
        UserDto userDto = authService.findUserByEmail(loginRequest.getEmail());
        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        response.addCookie(cookieService.deleteCookie("jwt"));
        return new ResponseEntity<>("Logged out successfully", HttpStatus.OK);
    }

    @GetMapping("/active/{id}/{token}")
    public ResponseEntity<?> active_account(@PathVariable Long id, @PathVariable String token, HttpServletResponse response)
            throws MessagingException, IOException {
        authService.activateUser(id, token);
        response.sendRedirect(redirectUrl);
        return new ResponseEntity<>("Account has been activated ", HttpStatus.OK);
    }

    @GetMapping("/check")
    public ResponseEntity<?> check(HttpServletRequest request){
        String token = cookieService.getJwtCookie(request);
        UserDto user = authService.verifyToken(token);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }
}
