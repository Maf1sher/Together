package org.mafisher.togetherbackend.service;

import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mafisher.togetherbackend.dto.UserDto;
import org.mafisher.togetherbackend.dto.request.LoginRequest;
import org.mafisher.togetherbackend.dto.request.RegisterRequest;
import org.mafisher.togetherbackend.entity.ActivationToken;
import org.mafisher.togetherbackend.entity.Role;
import org.mafisher.togetherbackend.entity.User;
import org.mafisher.togetherbackend.handler.BusinessErrorCodes;
import org.mafisher.togetherbackend.handler.CustomException;
import org.mafisher.togetherbackend.mappers.Mapper;
import org.mafisher.togetherbackend.repository.RolesRepository;
import org.mafisher.togetherbackend.repository.UserRepository;
import org.mafisher.togetherbackend.service.impl.AuthServiceImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private Mapper<User, UserDto> mapper;
    @Mock private RolesRepository rolesRepository;
    @Mock private EmailService emailService;
    @Mock private ActivationTokenService activationTokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void createUser_WithNewData_ReturnsUserDto() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "John", "Doe", "johndoe", "john@example.com", "password"
        );

        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.findByNickName(any())).thenReturn(Optional.empty());
        when(rolesRepository.findByName("USER")).thenReturn(Optional.of(Role.builder().name("USER").build()));
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(activationTokenService.getActivationTokenByUser(any())).thenReturn(new ActivationToken());

        UserDto result = authService.createUser(request);

        verify(userRepository).save(argThat(user ->
                user.getEmail().equals("john@example.com") &&
                        !user.isEnabled() &&
                        user.getPassword().equals("encodedPassword")
        ));
        verify(activationTokenService).createActivationToken(any());
        verify(emailService).sendEmail(any(), any(), any(), any(), any());
    }

    @Test
    void createUser_WithExistingEmail_ThrowsException() {
        RegisterRequest request = new RegisterRequest(
                "test", "test", "test", "test@mail.com", "test"
        );
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authService.createUser(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", BusinessErrorCodes.EMAIL_IS_USED);
    }

    @Test
    void verify_WithValidCredentials_ReturnsToken() throws MessagingException {
        LoginRequest request = new LoginRequest("john@example.com", "password");
        User user = new User();
        user.setEnable(true);
        Authentication authentication = mock(Authentication.class);

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateToken(any())).thenReturn("jwtToken");
        when(authentication.isAuthenticated()).thenReturn(true);

        String token = authService.verify(request);

        assertThat(token).isEqualTo("jwtToken");
    }

    @Test
    void verify_WithDisabledAccount_ThrowsException() {
        User user = new User();
        user.setEnable(false);
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.verify(new LoginRequest("test", "test")))
            .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", BusinessErrorCodes.ACCOUNT_DISABLED);
    }

    @Test
    void activateUser_WithValidToken_ActivatesAccount() throws MessagingException {
        User user = new User();
        user.setEnable(false);
        ActivationToken token = ActivationToken.builder()
                .token("valid-token")
                .expiryDate(Instant.now().plusSeconds(60))
                .build();

        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(activationTokenService.getActivationTokenByUser(any())).thenReturn(token);

        authService.activateUser(1L, "valid-token");

        assertThat(user.isEnabled()).isTrue();
        verify(activationTokenService).deleteActivationToken(token);
    }

    @Test
    void activateUser_WithExpiredToken_ResendsEmail() throws MessagingException {
        User user = new User();
        ActivationToken expiredToken = ActivationToken.builder()
                .token("expired")
                .expiryDate(Instant.now().minusSeconds(60))
                .build();

        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(activationTokenService.getActivationTokenByUser(any())).thenReturn(expiredToken);
        when(activationTokenService.verifyExpiration(expiredToken)).thenReturn(true);

        assertThatThrownBy(() -> authService.activateUser(1L, "expired"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", BusinessErrorCodes.TOKEN_EXPIRED);

        verify(emailService).sendEmail(any(), any(), any(), any(), any());
        verify(activationTokenService).deleteActivationToken(expiredToken);
    }

    @Test
    void verifyToken_WithValidToken_ReturnsUserDto() {
        when(jwtService.validateJwtToken("valid")).thenReturn(true);
        when(jwtService.extractUserName("valid")).thenReturn("john@example.com");
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(new User()));
        when(mapper.mapTo(any())).thenReturn(new UserDto());

        UserDto result = authService.verifyToken("valid");

        assertThat(result).isNotNull();
    }

    @Test
    void findUserByEmail_WhenExists_ReturnsDto() {
        User user = new User();
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(mapper.mapTo(user)).thenReturn(new UserDto());

        UserDto result = authService.findUserByEmail("john@example.com");

        assertThat(result).isNotNull();
    }
}
