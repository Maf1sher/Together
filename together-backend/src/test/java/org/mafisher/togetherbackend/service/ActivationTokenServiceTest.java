package org.mafisher.togetherbackend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mafisher.togetherbackend.entity.ActivationToken;
import org.mafisher.togetherbackend.entity.User;
import org.mafisher.togetherbackend.repository.ActivationTokenRepository;
import org.mafisher.togetherbackend.service.impl.ActivationTokenServiceImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ActivationTokenServiceTest {

    @Mock
    private ActivationTokenRepository activationTokenRepository;

    @InjectMocks
    private ActivationTokenServiceImpl activationTokenService;

    @Test
    void createActivationToken_GeneratesValidToken() {
        User user = new User();
        when(activationTokenRepository.save(any(ActivationToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ActivationToken token = activationTokenService.createActivationToken(user);

        assertThat (token.getToken())
                .hasSize(20)
                .containsOnlyDigits();
        assertEquals(token.getUser(), user);
        assertThat(token.getExpiryDate()).isAfter(Instant.now());
        verify(activationTokenRepository).save(token);
    }

    @Test
    void getActivationTokenByUser_WhenTokenExists_ReturnsToken() {
        User user = new User();
        ActivationToken expectedToken = new ActivationToken();
        when(activationTokenRepository.findByUser(user)).thenReturn(Optional.of(expectedToken));

        ActivationToken result = activationTokenService.getActivationTokenByUser(user);

        assertThat(result).isEqualTo(expectedToken);
    }

    @Test
    void getActivationTokenByUser_WhenTokenNotExists_ThrowsException() {
        User user = new User();
        when(activationTokenRepository.findByUser(user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> activationTokenService.getActivationTokenByUser(user))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    @Test
    void deleteActivationToken_CallsRepositoryDelete() {
        ActivationToken token = new ActivationToken();
        activationTokenService.deleteActivationToken(token);

        verify(activationTokenRepository).delete(token);
    }

    @Test
    void verifyExpiration_WhenTokenExpired_ReturnsTrue() {
        ActivationToken expiredToken = new ActivationToken();
        expiredToken.setExpiryDate(Instant.now().minusSeconds(60));

        boolean isExpired = activationTokenService.verifyExpiration(expiredToken);

        assertThat(isExpired).isTrue();
    }

    @Test
    void verifyExpiration_WhenTokenNotExpired_ReturnsFalse() {
        ActivationToken validToken = new ActivationToken();
        validToken.setExpiryDate(Instant.now().plusSeconds(60));

        boolean isExpired = activationTokenService.verifyExpiration(validToken);

        assertThat(isExpired).isFalse();
    }
}
