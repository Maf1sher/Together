package org.mafisher.togetherbackend.repository;

import org.mafisher.togetherbackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByNickName(String username);
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM user u WHERE " +
            "LOWER(u.nickName) LIKE LOWER(CONCAT('%', :query, '%')) AND u <> :currentUser " +
            "AND NOT EXISTS (SELECT f FROM Friendship f WHERE " +
            "(f.user1 = :currentUser AND f.user2 = u) OR (f.user2 = :currentUser AND f.user1 = u))")
    Page<User> findPotentialFriends(@Param("query") String query, @Param("currentUser") User currentUser, Pageable pageable);
}
