package org.mafisher.togetherbackend.repository;

import org.mafisher.togetherbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByNickName(String username);
    Optional<User> findByEmail(String email);
}
