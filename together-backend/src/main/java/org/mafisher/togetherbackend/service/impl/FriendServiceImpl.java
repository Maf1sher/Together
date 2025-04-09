package org.mafisher.togetherbackend.service.impl;

import lombok.RequiredArgsConstructor;
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
import org.mafisher.togetherbackend.service.FriendService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mafisher.togetherbackend.enums.FriendRequestStatus.*;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {
    private final FriendRequestRepository friendRequestRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final Mapper<User, UserDto> mapper;

    @Override
    public void sendRequest(String nickname, Principal principal) {
        User sender = checkUserPrincipal(principal);

        User receiver = checkUserExist(nickname);

        checkUsersAreFriends(sender, receiver);

        isUsersDifferent(sender, receiver);

        friendRequestRepository.findBySenderAndReceiver(sender, receiver).ifPresent(req -> {
            if (req.getStatus() == PENDING) throw new CustomException(BusinessErrorCodes.REQUEST_ALREADY_EXISTS);
        });

        Optional<FriendRequest> friendRequest = friendRequestRepository.findBySenderAndReceiver(receiver, sender);

        if(friendRequest.isPresent() && friendRequest.get().getStatus() == PENDING) {
            friendRequest.get().setStatus(ACCEPT);
            friendRequestRepository.save(friendRequest.get());

            Friendship friendship = Friendship.builder()
                    .user1(sender)
                    .user2(receiver)
                    .createdAt(LocalDateTime.now())
                    .build();
            friendshipRepository.save(friendship);
            return;
        }

        FriendRequest request = FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .createdAt(LocalDateTime.now())
                .status(PENDING)
                .build();
        friendRequestRepository.save(request);
    }

    @Override
    public void acceptRequest(String nickname, Principal principal) {
        User sender = checkUserPrincipal(principal);

        User acceptedUser = checkUserExist(nickname);

        checkUsersAreFriends(sender, acceptedUser);

        FriendRequest friendRequest = findFriendRequest(acceptedUser, sender);

        if(friendRequest.getStatus() != PENDING)
            throw new CustomException(BusinessErrorCodes.REQUEST_NOT_PENDING);

        friendRequest.setStatus(ACCEPT);
        friendRequestRepository.save(friendRequest);

        Friendship friendship = new Friendship();
        friendship.setUser1(sender);
        friendship.setUser2(acceptedUser);
        friendship.setCreatedAt(LocalDateTime.now());
        friendshipRepository.save(friendship);
    }

    @Override
    public void rejectRequest(String nickname, Principal principal) {
        User sender = checkUserPrincipal(principal);

        User rejectedUser = checkUserExist(nickname);

        checkUsersAreFriends(sender, rejectedUser);

        FriendRequest friendRequest = findFriendRequest(sender, rejectedUser);

        if(friendRequest.getStatus() != PENDING)
            throw new CustomException(BusinessErrorCodes.REQUEST_NOT_PENDING);

        friendRequest.setStatus(REJECT);
        friendRequestRepository.save(friendRequest);
    }

    @Override
    public List<UserDto> getFriends(Principal principal, Pageable pageable) {
        User sender = checkUserPrincipal(principal);
        return friendshipRepository.findFriendsByUser(sender, pageable).stream().map(mapper::mapTo)
                .toList();
    }

    @Override
    public List<UserDto> getReceivedRequests(Principal principal, Pageable pageable) {
        User sender = checkUserPrincipal(principal);
        return sender.getReceivedRequests().stream()
                .filter((request)-> request.getStatus() == PENDING)
                .map((request)-> mapper.mapTo(request.getSender()))
                .toList();
    }

    private User checkUserPrincipal(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new CustomException(BusinessErrorCodes.BAD_CREDENTIALS));
    }

    private User checkUserExist(String nickname) {
        return userRepository.findByNickName(nickname)
                .orElseThrow(() -> new CustomException(BusinessErrorCodes.USER_NOT_FOUND));
    }

    private void checkUsersAreFriends(User user1, User user2) {
        if (friendshipRepository.existsBetweenUsers(user1, user2)) {
            throw new CustomException(BusinessErrorCodes.USERS_ARE_ALREADY_FRIENDS);
        }
    }

    private FriendRequest findFriendRequest(User user1, User user2) {
        return friendRequestRepository.findBySenderAndReceiver(user1, user2)
                .orElseThrow(() -> new CustomException(BusinessErrorCodes.REQUEST_NOT_FOUND));
    }

    private void isUsersDifferent(User user1, User user2) {
        if(user1.getId().equals(user2.getId()))
            throw new CustomException(BusinessErrorCodes.USERS_ARE_THE_SAME);
    }
}
