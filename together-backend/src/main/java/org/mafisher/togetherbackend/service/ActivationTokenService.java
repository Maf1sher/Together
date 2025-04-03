package org.mafisher.togetherbackend.service;


import org.mafisher.togetherbackend.entity.ActivationToken;
import org.mafisher.togetherbackend.entity.User;

public interface ActivationTokenService {
    ActivationToken createActivationToken(User user);
    ActivationToken getActivationTokenByUser(User user);
    void deleteActivationToken(ActivationToken token);
    boolean verifyExpiration(ActivationToken token);
}
