package org.mafisher.togetherbackend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mafisher.togetherbackend.dto.UserDto;
import org.mafisher.togetherbackend.entity.FriendRequest;
import org.mafisher.togetherbackend.entity.Friendship;
import org.mafisher.togetherbackend.entity.User;
import org.mafisher.togetherbackend.handler.BusinessErrorCodes;
import org.mafisher.togetherbackend.handler.CustomException;
import org.mafisher.togetherbackend.mappers.Mapper;
import org.mafisher.togetherbackend.repository.FriendRequestRepository;
import org.mafisher.togetherbackend.repository.FriendshipRepository;
import org.mafisher.togetherbackend.repository.UserRepository;
import org.mafisher.togetherbackend.service.impl.FriendServiceImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mafisher.togetherbackend.enums.FriendRequestStatus.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendServiceTest {

    @Mock
    private FriendRequestRepository friendRequestRepository;

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Mapper<User, UserDto> mapper;

    @InjectMocks
    private FriendServiceImpl friendService;

    @Mock
    private Principal principal;

    private final User userA = User.builder().id(1L).email("a@test.com").nickName("UserA").build();
    private final User userB = User.builder().id(2L).email("b@test.com").nickName("UserB").build();
    private final UserDto userBDto = UserDto.builder().id(2L).email("b@test.com").nickName("UserB").build();

    @Test
    void sendRequest_ShouldCreateFriendship_WhenReverseRequestExists() {
        when(principal.getName()).thenReturn("a@test.com");
        when(userRepository.findByEmail("a@test.com")).thenReturn(Optional.of(userA));
        when(userRepository.findByNickName("UserB")).thenReturn(Optional.of(userB));
        when(friendshipRepository.existsBetweenUsers(userA, userB)).thenReturn(false);

        FriendRequest existingRequest = FriendRequest.builder()
                .sender(userB)
                .receiver(userA)
                .status(PENDING)
                .build();
        when(friendRequestRepository.findBySenderAndReceiver(eq(userB), eq(userA)))
                .thenReturn(Optional.of(existingRequest));
        when(friendRequestRepository.findBySenderAndReceiver(eq(userA), eq(userB)))
                .thenReturn(Optional.empty());

        friendService.sendRequest("UserB", principal);

        assertThat(existingRequest.getStatus()).isEqualTo(ACCEPT);
        verify(friendRequestRepository).save(existingRequest);
        verify(friendshipRepository).save(any(Friendship.class));
    }

    @Test
    void sendRequest_ShouldThrow_WhenUsersAreSame() {
        when(principal.getName()).thenReturn("a@test.com");
        when(userRepository.findByEmail("a@test.com")).thenReturn(Optional.of(userA));
        when(userRepository.findByNickName("UserA")).thenReturn(Optional.of(userA));

        assertThatThrownBy(() -> friendService.sendRequest("UserA", principal))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", BusinessErrorCodes.USERS_ARE_THE_SAME);
    }

    @Test
    void acceptRequest_ShouldCreateFriendship() {
        when(principal.getName()).thenReturn("a@test.com");
        when(userRepository.findByEmail("a@test.com")).thenReturn(Optional.of(userA));
        when(userRepository.findByNickName("UserB")).thenReturn(Optional.of(userB));
        when(friendshipRepository.existsBetweenUsers(userA, userB)).thenReturn(false);

        FriendRequest request = FriendRequest.builder()
                .sender(userB)
                .receiver(userA)
                .status(PENDING)
                .build();
        when(friendRequestRepository.findBySenderAndReceiver(userB, userA))
                .thenReturn(Optional.of(request));

        friendService.acceptRequest("UserB", principal);

        assertThat(request.getStatus()).isEqualTo(ACCEPT);
        verify(friendshipRepository).save(argThat(f ->
                (f.getUser1() == userA && f.getUser2() == userB) ||
                        (f.getUser1() == userB && f.getUser2() == userA)
        ));
    }

    @Test
    void rejectRequest_ShouldUpdateStatus() {
        when(principal.getName()).thenReturn("a@test.com");
        when(userRepository.findByEmail("a@test.com")).thenReturn(Optional.of(userA));
        when(userRepository.findByNickName("UserB")).thenReturn(Optional.of(userB));
        when(friendshipRepository.existsBetweenUsers(userA, userB)).thenReturn(false);

        FriendRequest request = FriendRequest.builder()
                .sender(userB)
                .receiver(userA)
                .status(PENDING)
                .build();
        when(friendRequestRepository.findBySenderAndReceiver(userB, userA))
                .thenReturn(Optional.of(request));

        friendService.rejectRequest("UserB", principal);

        assertThat(request.getStatus()).isEqualTo(REJECT);
        verify(friendRequestRepository).save(request);
    }

    @Test
    void getFriends_ShouldReturnMappedDtos() {
        when(principal.getName()).thenReturn("a@test.com");
        when(userRepository.findByEmail("a@test.com")).thenReturn(Optional.of(userA));

        Page<User> friendsPage = new PageImpl<>(List.of(userB));
        when(friendshipRepository.findFriendsByUser(userA, Pageable.unpaged()))
                .thenReturn(friendsPage);
        when(mapper.mapTo(userB)).thenReturn(userBDto);

        List<UserDto> result = friendService.getFriends(principal, Pageable.unpaged());
        assertThat(result).containsExactly(userBDto);
    }

    @Test
    void getReceivedRequests_ShouldFilterPending() {
        // Arrange
        when(principal.getName()).thenReturn("a@test.com");
        User userAWithRequests = User.builder()
                .receivedRequests(List.of(
                        FriendRequest.builder().sender(userB).status(PENDING).build(),
                        FriendRequest.builder().sender(userB).status(ACCEPT).build()
                ))
                .build();
        when(userRepository.findByEmail("a@test.com")).thenReturn(Optional.of(userAWithRequests));
        when(mapper.mapTo(userB)).thenReturn(userBDto);

        List<UserDto> result = friendService.getReceivedRequests(principal, Pageable.unpaged());

        assertThat(result).hasSize(1).containsExactly(userBDto);
    }

    @Test
    void searchUsers_ShouldMapResults() {
        when(principal.getName()).thenReturn("a@test.com");
        when(userRepository.findByEmail("a@test.com")).thenReturn(Optional.of(userA));

        Page<User> usersPage = new PageImpl<>(List.of(userB));
        when(userRepository.findPotentialFriends("query", userA, Pageable.unpaged()))
                .thenReturn(usersPage);
        when(mapper.mapTo(userB)).thenReturn(userBDto);
        Page<UserDto> result = friendService.searchUsers("query", Pageable.unpaged(), principal);

        assertThat(result.getContent()).containsExactly(userBDto);
    }
}
