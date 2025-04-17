package org.mafisher.togetherbackend.service.impl;

import lombok.RequiredArgsConstructor;
import org.mafisher.togetherbackend.entity.User;
import org.mafisher.togetherbackend.handler.BusinessErrorCodes;
import org.mafisher.togetherbackend.handler.CustomException;
import org.mafisher.togetherbackend.repository.UserRepository;
import org.mafisher.togetherbackend.service.PrincipalService;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class PrincipalServiceImpl implements PrincipalService {

    private final UserRepository userRepository;

    @Override
    public User checkUserPrincipal(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new CustomException(BusinessErrorCodes.BAD_CREDENTIALS));
    }

    @Override
    public User checkUserExist(String nickname) {
        return userRepository.findByNickName(nickname)
                .orElseThrow(() -> new CustomException(BusinessErrorCodes.USER_NOT_FOUND));
    }

    @Override
    public boolean isUsersDifferent(User user1, User user2) {
        if(user1.getId().equals(user2.getId()))
            throw new CustomException(BusinessErrorCodes.USERS_ARE_THE_SAME);
        return true;
    }
}
