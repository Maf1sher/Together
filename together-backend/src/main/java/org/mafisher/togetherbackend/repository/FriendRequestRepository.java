package org.mafisher.togetherbackend.repository;

import org.mafisher.togetherbackend.entity.FriendRequest;
import org.mafisher.togetherbackend.entity.User;
import org.mafisher.togetherbackend.enums.FriendRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    Page<FriendRequest> findByReceiverAndStatus(User receiver, FriendRequestStatus status, Pageable pageable);
    Optional<FriendRequest> findBySenderAndReceiver(User sender, User receiver);
}

