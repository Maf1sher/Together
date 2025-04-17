package org.mafisher.togetherbackend.repository;

import org.mafisher.togetherbackend.entity.Room;
import org.mafisher.togetherbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByOwnerAndName(User owner, String name);
    List<Room> findByOwner(User owner);

    @Query("SELECT r FROM Room r WHERE r.owner = :user OR :user MEMBER OF r.participants")
    List<Room> findRoomsByUser(@Param("user") User user);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM Room r WHERE r = :room AND (r.owner = :user OR :user MEMBER OF r.participants)")
    boolean isUserInRoom(@Param("room") Room room, @Param("user") User user);
}
