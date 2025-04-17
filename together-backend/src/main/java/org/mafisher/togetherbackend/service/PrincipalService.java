package org.mafisher.togetherbackend.service;

import org.mafisher.togetherbackend.entity.User;

import java.security.Principal;

public interface PrincipalService {
    User checkUserPrincipal(Principal principal);
    User checkUserExist(String nickname);
    boolean isUsersDifferent(User user1, User user2);
}
