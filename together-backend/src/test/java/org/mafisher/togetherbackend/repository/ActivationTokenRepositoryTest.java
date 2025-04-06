package org.mafisher.togetherbackend.repository;

import org.junit.jupiter.api.Test;
import org.mafisher.togetherbackend.entity.ActivationToken;
import org.mafisher.togetherbackend.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class ActivationTokenRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ActivationTokenRepository activationTokenRepository;

    @Test
    void whenFindByUser_thenReturnActivationToken() {
        User user = User.builder()
                .email("user@example.com")
                .password("password")
                .build();
        entityManager.persist(user);

        ActivationToken token = ActivationToken.builder()
                .token("test-token")
                .expiryDate(Instant.now().plusSeconds(3600))
                .user(user)
                .build();
        entityManager.persist(token);
        entityManager.flush();

        Optional<ActivationToken> found = activationTokenRepository.findByUser(user);

        assertTrue(found.isPresent());
        assertEquals("test-token", found.get().getToken());
        assertEquals(user.getId(), found.get().getUser().getId());
    }

    @Test
    void whenFindByUserWithNoToken_thenReturnEmpty() {
        User user = User.builder()
                .email("user@example.com")
                .password("password")
                .build();
        entityManager.persist(user);
        entityManager.flush();

        Optional<ActivationToken> found = activationTokenRepository.findByUser(user);

        assertFalse(found.isPresent());
    }

    @Test
    void whenSaveActivationToken_thenTokenShouldBePersisted() {
        User user = User.builder()
                .email("user@example.com")
                .password("password")
                .build();
        entityManager.persist(user);

        ActivationToken token = ActivationToken.builder()
                .token("new-token")
                .expiryDate(Instant.now().plusSeconds(3600))
                .user(user)
                .build();

        ActivationToken savedToken = activationTokenRepository.save(token);

        assertNotNull(savedToken.getId());
        assertEquals("new-token", savedToken.getToken());
        assertEquals(user.getId(), savedToken.getUser().getId());

        ActivationToken foundToken = entityManager.find(ActivationToken.class, savedToken.getId());
        assertEquals(savedToken.getToken(), foundToken.getToken());
    }

    @Test
    void whenDeleteActivationToken_thenTokenShouldBeRemoved() {
        User user = User.builder()
                .email("user@example.com")
                .password("password")
                .build();
        entityManager.persist(user);

        ActivationToken token = ActivationToken.builder()
                .token("to-delete")
                .expiryDate(Instant.now().plusSeconds(3600))
                .user(user)
                .build();
        entityManager.persist(token);
        entityManager.flush();

        activationTokenRepository.delete(token);

        ActivationToken deletedToken = entityManager.find(ActivationToken.class, token.getId());
        assertNull(deletedToken);
    }
}
