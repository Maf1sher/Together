package org.mafisher.togetherbackend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity(name = "role")
@Table(name = "role")
@Data
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;
}
