package org.mafisher.togetherbackend.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mafisher.togetherbackend.entity.Friendship;
import org.mafisher.togetherbackend.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class FriendshipRepositoryTest {

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User userA;
    private User userB;
    private User userC;
    private User userD;

    @BeforeEach
    void setUp() {
        userA = createUser("userA@test.com", "UserA");
        userB = createUser("userB@test.com", "UserB");
        userC = createUser("userC@test.com", "UserC");
        userD = createUser("userD@test.com", "UserD");

        createFriendship(userA, userB);
        createFriendship(userA, userC);
        createFriendship(userB, userC);
    }

    private User createUser(String email, String nick) {
        User user = User.builder()
                .email(email)
                .nickName(nick)
                .password("password")
                .enable(true)
                .build();
        return entityManager.persist(user);
    }

    private void createFriendship(User u1, User u2) {
        Friendship friendship = Friendship.builder()
                .user1(u1)
                .user2(u2)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(friendship);
    }


    @Test
    void findByUser_ShouldReturnAllFriendshipsForUser() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Friendship> result = friendshipRepository.findByUser(userA, pageable);

        assertThat(result.getContent())
                .hasSize(2)
                .allMatch(f -> f.getUser1() == userA || f.getUser2() == userA);
    }

    @Test
    void findFriendsByUser_ShouldReturnUniqueFriends() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<User> result = friendshipRepository.findFriendsByUser(userA, pageable);

        assertThat(result.getContent())
                .hasSize(2)
                .containsExactlyInAnyOrder(userB, userC);
    }

    @Test
    void findFriendsByUser_WithPagination_ShouldReturnPaginatedResults() {
        Pageable pageable = PageRequest.of(0, 1);

        Page<User> result = friendshipRepository.findFriendsByUser(userB, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    void existsBetweenUsers_WhenFriendsExist_ShouldReturnTrue() {
        assertThat(friendshipRepository.existsBetweenUsers(userA, userB)).isTrue();
        assertThat(friendshipRepository.existsBetweenUsers(userB, userA)).isTrue();
        assertThat(friendshipRepository.existsBetweenUsers(userB, userC)).isTrue();
    }

    @Test
    void existsBetweenUsers_WhenNoFriendship_ShouldReturnFalse() {
        assertThat(friendshipRepository.existsBetweenUsers(userA, userD)).isFalse();
        assertThat(friendshipRepository.existsBetweenUsers(userD, userC)).isFalse();
    }

    @Test
    void existsBetweenUsers_WithSameUser_ShouldReturnFalse() {
        assertThat(friendshipRepository.existsBetweenUsers(userA, userA)).isFalse();
    }
}
