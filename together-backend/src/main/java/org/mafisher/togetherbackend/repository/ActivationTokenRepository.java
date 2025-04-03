package org.mafisher.togetherbackend.repository;


import org.mafisher.togetherbackend.entity.ActivationToken;
import org.mafisher.togetherbackend.entity.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ActivationTokenRepository extends CrudRepository<ActivationToken, Long> {
    Optional<ActivationToken> findByUser(User user);
}
