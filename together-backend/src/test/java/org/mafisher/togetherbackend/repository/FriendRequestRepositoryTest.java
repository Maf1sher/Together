package org.mafisher.togetherbackend.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mafisher.togetherbackend.entity.FriendRequest;
import org.mafisher.togetherbackend.entity.User;
import org.mafisher.togetherbackend.enums.FriendRequestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class FriendRequestRepositoryTest {

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User sender1;
    private User receiver1;
    private User receiver2;

    @BeforeEach
    void setUp() {
        sender1 = createAndPersistUser("sender1");
        receiver1 = createAndPersistUser("receiver1");
        receiver2 = createAndPersistUser("receiver2");

        createAndPersistFriendRequest(sender1, receiver1, FriendRequestStatus.PENDING);
        createAndPersistFriendRequest(sender1, receiver2, FriendRequestStatus.ACCEPT);
        createAndPersistFriendRequest(sender1, receiver2, FriendRequestStatus.PENDING);
        createAndPersistFriendRequest(sender1, receiver2, FriendRequestStatus.PENDING);
    }

    private User createAndPersistUser(String nickname) {
        User user = new User();
        user.setNickName(nickname);
        return entityManager.persist(user);
    }

    private void createAndPersistFriendRequest(User sender, User receiver, FriendRequestStatus status) {
        FriendRequest fr = FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(fr);
    }

    @Test
    void findByReceiverAndStatus_ShouldReturnMatchingRequests() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<FriendRequest> result = friendRequestRepository.findByReceiverAndStatus(receiver1, FriendRequestStatus.PENDING, pageable);

        assertThat(result.getContent()).hasSize(1);

        assert result.getContent().get(0).getReceiver().equals(receiver1);
        assert result.getContent().get(0).getStatus().equals(FriendRequestStatus.PENDING);
    }

    @Test
    void findByReceiverAndStatus_WithPaging_ShouldReturnPaginatedResults() {
        Pageable pageable = PageRequest.of(0, 1);

        Page<FriendRequest> result = friendRequestRepository.findByReceiverAndStatus(receiver2, FriendRequestStatus.PENDING, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    void findBySenderAndReceiver_WhenExists_ShouldReturnRequest() {
        Optional<FriendRequest> result = friendRequestRepository.findBySenderAndReceiver(sender1, receiver1);

        assertThat(result).isPresent();
        assertThat(result.get()).satisfies(fr -> {
            assertThat(fr.getSender()).isEqualTo(sender1);
            assertThat(fr.getReceiver()).isEqualTo(receiver1);
        });
    }

    @Test
    void findBySenderAndReceiver_WhenNotExists_ShouldReturnEmpty() {
        User newUser = createAndPersistUser("newUser");

        Optional<FriendRequest> result = friendRequestRepository.findBySenderAndReceiver(newUser, receiver1);

        assertThat(result).isEmpty();
    }
}
