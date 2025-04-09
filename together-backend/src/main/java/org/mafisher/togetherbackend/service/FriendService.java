package org.mafisher.togetherbackend.service;

import org.mafisher.togetherbackend.dto.UserDto;
import org.springframework.data.domain.Pageable;

import java.security.Principal;
import java.util.List;

public interface FriendService {
    void sendRequest(String nickname, Principal principal);
    void acceptRequest(String nickname, Principal principal);
    void rejectRequest(String nickname, Principal principal);
    List<UserDto> getFriends(Principal principal, Pageable pageable);
}
