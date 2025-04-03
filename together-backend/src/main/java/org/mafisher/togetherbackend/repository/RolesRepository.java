package org.mafisher.togetherbackend.repository;


import org.mafisher.togetherbackend.entity.Role;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RolesRepository extends CrudRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
