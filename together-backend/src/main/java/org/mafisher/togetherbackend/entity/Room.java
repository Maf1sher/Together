package org.mafisher.togetherbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    private User owner;

    @ManyToMany
    private Set<User> participants = new HashSet<>();

    public void addParticipant(User user) {
        if (participants == null) {
            participants = new HashSet<>();
        }
        participants.add(user);
    }

    public void removeParticipant(User user) {
        if (participants != null) {
            participants.remove(user);
        }
    }

}