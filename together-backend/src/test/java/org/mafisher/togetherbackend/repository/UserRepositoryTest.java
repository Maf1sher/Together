package org.mafisher.togetherbackend.repository;

import org.junit.jupiter.api.Test;
import org.mafisher.togetherbackend.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFindByNickName() {
        User user = User.builder()
                .nickName("testUser")
                .email("test@mail.com")
                .build();
        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByNickName("testUser");

        assert  foundUser.isPresent();
        assertEquals(foundUser.get().getEmail(),("test@mail.com"));
    }

    @Test
    public void testFindByEmail() {
        User user = User.builder()
                .nickName("testUser2")
                .email("test2@mail.com")
                .build();
        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByEmail("test2@mail.com");

        assert foundUser.isPresent();
        assertEquals(foundUser.get().getEmail(),"test2@mail.com");
    }
}
