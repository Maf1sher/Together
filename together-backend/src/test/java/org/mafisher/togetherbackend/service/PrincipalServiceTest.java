package org.mafisher.togetherbackend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mafisher.togetherbackend.entity.User;
import org.mafisher.togetherbackend.handler.BusinessErrorCodes;
import org.mafisher.togetherbackend.handler.CustomException;
import org.mafisher.togetherbackend.repository.UserRepository;
import org.mafisher.togetherbackend.service.impl.PrincipalServiceImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.security.Principal;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrincipalServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PrincipalServiceImpl principalService;

    private final User testUser = User.builder()
            .id(1L)
            .email("test@example.com")
            .nickName("testUser")
            .build();

    @Test
    void checkUserPrincipal_ShouldReturnUser_WhenPrincipalIsValid() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        User result = principalService.checkUserPrincipal(principal);

        assertThat(result).isEqualTo(testUser);
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void checkUserPrincipal_ShouldThrow_WhenUserNotFound() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("invalid@example.com");
        when(userRepository.findByEmail("invalid@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> principalService.checkUserPrincipal(principal))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", BusinessErrorCodes.BAD_CREDENTIALS);
    }

    @Test
    void checkUserExist_ShouldReturnUser_WhenNicknameExists() {
        when(userRepository.findByNickName("validNickname"))
                .thenReturn(Optional.of(testUser));

        User result = principalService.checkUserExist("validNickname");

        assertThat(result).isEqualTo(testUser);
        verify(userRepository).findByNickName("validNickname");
    }

    @Test
    void checkUserExist_ShouldThrow_WhenNicknameNotFound() {
        when(userRepository.findByNickName("invalidNickname"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> principalService.checkUserExist("invalidNickname"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", BusinessErrorCodes.USER_NOT_FOUND);
    }

    @Test
    void isUsersDifferent_ShouldThrow_WhenSameUser() {
        User user1 = User.builder().id(1L).build();
        User user2 = User.builder().id(1L).build();

        assertThatThrownBy(() -> principalService.isUsersDifferent(user1, user2))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", BusinessErrorCodes.USERS_ARE_THE_SAME);
    }

    @Test
    void isUsersDifferent_ShouldReturnTrue_WhenDifferentUsers() {
        User user1 = User.builder().id(1L).build();
        User user2 = User.builder().id(2L).build();

        assertThat(principalService.isUsersDifferent(user1, user2)).isTrue();
    }
}
