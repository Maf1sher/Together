package org.mafisher.togetherbackend.repository;

import org.mafisher.togetherbackend.entity.Friendship;
import org.mafisher.togetherbackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    @Query("SELECT f FROM Friendship f WHERE f.user1 = :user OR f.user2 = :user")
    Page<Friendship> findByUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT f.user2 FROM Friendship f WHERE f.user1 = :user " +
            "UNION " +
            "SELECT f.user1 FROM Friendship f WHERE f.user2 = :user")
    Page<User> findFriendsByUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN TRUE ELSE FALSE END FROM Friendship f" +
            " WHERE (f.user1 = :u1 AND f.user2 = :u2) OR (f.user1 = :u2 AND f.user2 = :u1)")
    boolean existsBetweenUsers(@Param("u1") User u1, @Param("u2") User u2);
}
