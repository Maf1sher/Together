package org.mafisher.togetherbackend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mafisher.togetherbackend.dto.UserDto;
import org.mafisher.togetherbackend.entity.FriendRequest;
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

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mafisher.togetherbackend.enums.FriendRequestStatus.ACCEPT;
import static org.mafisher.togetherbackend.enums.FriendRequestStatus.PENDING;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendServiceTest {

    @Mock
    private FriendRequestRepository friendRequestRepository;

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private PrincipalService principalService;

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
    private final UserDto userBDto = UserDto.builder().nickName("UserB").build();

    @Test
    void sendRequest_ShouldCreateFriendship_WhenReverseRequestExists() {
        when(principalService.checkUserPrincipal(principal)).thenReturn(userA);
        when(principalService.checkUserExist("UserB")).thenReturn(userB);
        when(principalService.isUsersDifferent(userA, userB)).thenReturn(true);
        when(friendshipRepository.existsBetweenUsers(userA, userB)).thenReturn(false);

        FriendRequest existingRequest = FriendRequest.builder()
                .sender(userB)
                .receiver(userA)
                .status(PENDING)
                .build();

        when(friendRequestRepository.findBySenderAndReceiver(userA, userB))
                .thenReturn(Optional.empty());
        when(friendRequestRepository.findBySenderAndReceiver(userB, userA))
                .thenReturn(Optional.of(existingRequest));

        friendService.sendRequest("UserB", principal);

        assertThat(existingRequest.getStatus()).isEqualTo(ACCEPT);
        verify(friendRequestRepository).save(existingRequest);
        verify(friendshipRepository).save(argThat(friendship ->
                friendship.getUser1().equals(userA) &&
                        friendship.getUser2().equals(userB)
        ));
    }

    @Test
    void sendRequest_ShouldThrow_WhenUsersAreSame() {
        when(principalService.checkUserPrincipal(principal)).thenReturn(userA);
        when(principalService.checkUserExist("UserA")).thenReturn(userA);

        doThrow(new CustomException(BusinessErrorCodes.USERS_ARE_THE_SAME))
                .when(principalService).isUsersDifferent(userA, userA);

        assertThatThrownBy(() -> friendService.sendRequest("UserA", principal))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", BusinessErrorCodes.USERS_ARE_THE_SAME);
    }

    @Test
    void acceptRequest_ShouldCreateFriendship() {
        when(principalService.checkUserPrincipal(principal)).thenReturn(userA);
        when(principalService.checkUserExist("UserB")).thenReturn(userB);
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
        verify(friendshipRepository).save(argThat(friendship ->
                (friendship.getUser1().equals(userA) && friendship.getUser2().equals(userB)) ||
                        (friendship.getUser1().equals(userB) && friendship.getUser2().equals(userA))
        ));
    }

    @Test
    void getFriends_ShouldReturnMappedDtos() {
        when(principalService.checkUserPrincipal(principal)).thenReturn(userA);
        Page<User> mockPage = new PageImpl<>(List.of(userB));
        when(friendshipRepository.findFriendsByUser(userA, Pageable.unpaged())).thenReturn(mockPage);
        when(mapper.mapTo(userB)).thenReturn(userBDto);

        List<UserDto> result = friendService.getFriends(principal, Pageable.unpaged());

        assertThat(result).containsExactly(userBDto);
        verify(mapper).mapTo(userB);
    }

    @Test
    void sendRequest_ShouldThrow_WhenRequestAlreadyExists() {
        when(principalService.checkUserPrincipal(principal)).thenReturn(userA);
        when(principalService.checkUserExist("UserB")).thenReturn(userB);
        when(principalService.isUsersDifferent(userA, userB)).thenReturn(true);
        when(friendshipRepository.existsBetweenUsers(userA, userB)).thenReturn(false);

        FriendRequest existingRequest = FriendRequest.builder()
                .sender(userA)
                .receiver(userB)
                .status(PENDING)
                .build();
        when(friendRequestRepository.findBySenderAndReceiver(userA, userB))
                .thenReturn(Optional.of(existingRequest));

        assertThatThrownBy(() -> friendService.sendRequest("UserB", principal))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", BusinessErrorCodes.REQUEST_ALREADY_EXISTS);
    }

    @Test
    void sendRequest_ShouldCreateNewRequest_WhenNoExistingRequests() {
        when(principalService.checkUserPrincipal(principal)).thenReturn(userA);
        when(principalService.checkUserExist("UserB")).thenReturn(userB);
        when(principalService.isUsersDifferent(userA, userB)).thenReturn(true);
        when(friendshipRepository.existsBetweenUsers(userA, userB)).thenReturn(false);

        when(friendRequestRepository.findBySenderAndReceiver(any(), any()))
                .thenReturn(Optional.empty());

        friendService.sendRequest("UserB", principal);

        verify(friendRequestRepository).save(argThat(request ->
                request.getSender().equals(userA) &&
                        request.getReceiver().equals(userB) &&
                        request.getStatus() == PENDING
        ));
    }

    @Test
    void sendRequest_ShouldThrow_WhenUsersAreAlreadyFriends() {
        when(principalService.checkUserPrincipal(principal)).thenReturn(userA);
        when(principalService.checkUserExist("UserB")).thenReturn(userB);
        when(friendshipRepository.existsBetweenUsers(userA, userB)).thenReturn(true);

        assertThatThrownBy(() -> friendService.sendRequest("UserB", principal))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", BusinessErrorCodes.USERS_ARE_ALREADY_FRIENDS);
    }

    @Test
    void acceptRequest_ShouldThrow_WhenRequestNotPending() {
        when(principalService.checkUserPrincipal(principal)).thenReturn(userA);
        when(principalService.checkUserExist("UserB")).thenReturn(userB);

        FriendRequest request = FriendRequest.builder()
                .status(ACCEPT)
                .build();
        when(friendRequestRepository.findBySenderAndReceiver(userB, userA))
                .thenReturn(Optional.of(request));

        assertThatThrownBy(() -> friendService.acceptRequest("UserB", principal))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", BusinessErrorCodes.REQUEST_NOT_PENDING);
    }

    @Test
    void rejectRequest_ShouldThrow_WhenRequestNotFound() {
        when(principalService.checkUserPrincipal(principal)).thenReturn(userA);
        when(principalService.checkUserExist("UserB")).thenReturn(userB);
        when(friendRequestRepository.findBySenderAndReceiver(userB, userA))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> friendService.rejectRequest("UserB", principal))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", BusinessErrorCodes.REQUEST_NOT_FOUND);
    }

    @Test
    void searchUsers_ShouldReturnEmpty_WhenNoMatches() {
        when(principalService.checkUserPrincipal(principal)).thenReturn(userA);
        when(userRepository.findPotentialFriends(anyString(), eq(userA), any()))
                .thenReturn(Page.empty());

        Page<UserDto> result = friendService.searchUsers("invalid", Pageable.unpaged(), principal);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldThrowWhenPrincipalInvalid() {
        when(principalService.checkUserPrincipal(principal))
                .thenThrow(new CustomException(BusinessErrorCodes.BAD_CREDENTIALS));

        assertThatThrownBy(() -> friendService.getFriends(principal, Pageable.unpaged()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", BusinessErrorCodes.BAD_CREDENTIALS);
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(principalService.checkUserExist("InvalidUser"))
                .thenThrow(new CustomException(BusinessErrorCodes.USER_NOT_FOUND));

        assertThatThrownBy(() -> friendService.sendRequest("InvalidUser", principal))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", BusinessErrorCodes.USER_NOT_FOUND);
    }
}
