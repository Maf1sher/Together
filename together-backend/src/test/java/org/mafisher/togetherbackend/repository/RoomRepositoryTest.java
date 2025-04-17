package org.mafisher.togetherbackend.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mafisher.togetherbackend.entity.Room;
import org.mafisher.togetherbackend.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class RoomRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoomRepository roomRepository;

    private User owner;
    private User participant;
    private Room room1;
    private Room room2;

    @BeforeEach
    void setUp() {
        owner = entityManager.persist(User.builder().nickName("owner").build());
        participant = entityManager.persist(User.builder().nickName("participant").build());

        room1 = entityManager.persist(
                Room.builder()
                        .name("Room 1")
                        .owner(owner)
                        .participants(new HashSet<>(Set.of(participant)))
                        .build()
        );

        room2 = entityManager.persist(
                Room.builder()
                        .name("Room 2")
                        .owner(owner)
                        .build()
        );

        entityManager.flush();
    }

    @Test
    void findByOwnerAndName_ShouldReturnRoom() {
        Optional<Room> result = roomRepository.findByOwnerAndName(owner, "Room 1");

        assertThat(result).isPresent();
        assertThat(result.get()).extracting("name").isEqualTo("Room 1");
    }

    @Test
    void findByOwnerAndName_ShouldReturnEmptyWhenNotFound() {
        Optional<Room> result = roomRepository.findByOwnerAndName(owner, "NonExisting");

        assertThat(result).isEmpty();
    }

    @Test
    void findByOwner_ShouldReturnAllOwnedRooms() {
        List<Room> result = roomRepository.findByOwner(owner);

        assertThat(result)
                .hasSize(2)
                .extracting("name")
                .containsExactlyInAnyOrder("Room 1", "Room 2");
    }

    @Test
    void findRoomsByUser_ShouldReturnOwnedAndParticipatingRooms() {
        List<Room> ownerRooms = roomRepository.findRoomsByUser(owner);
        List<Room> participantRooms = roomRepository.findRoomsByUser(participant);

        assertThat(ownerRooms)
                .hasSize(2)
                .extracting("name")
                .containsExactlyInAnyOrder("Room 1", "Room 2");

        assertThat(participantRooms)
                .hasSize(1)
                .extracting("name")
                .containsExactly("Room 1");
    }

    @Test
    void isUserInRoom_ShouldCheckMembership() {
        boolean ownerInRoom1 = roomRepository.isUserInRoom(room1, owner);
        boolean participantInRoom1 = roomRepository.isUserInRoom(room1, participant);
        boolean strangerInRoom = roomRepository.isUserInRoom(room1,
                entityManager.persist(User.builder().nickName("stranger").build())
        );

        assertThat(ownerInRoom1).isTrue();
        assertThat(participantInRoom1).isTrue();
        assertThat(strangerInRoom).isFalse();
    }

    @Test
    void addAndRemoveParticipant_ShouldModifyCollection() {
        User newUser = entityManager.persist(User.builder().nickName("new").build());
        Room room = entityManager.find(Room.class, room1.getId());

        room.addParticipant(newUser);
        entityManager.flush();
        entityManager.refresh(room);

        assertThat(room.getParticipants())
                .hasSize(2)
                .containsExactlyInAnyOrder(participant, newUser);

        room.removeParticipant(newUser);
        entityManager.flush();
        entityManager.refresh(room);

        assertThat(room.getParticipants())
                .hasSize(1)
                .containsExactly(participant);
    }
}