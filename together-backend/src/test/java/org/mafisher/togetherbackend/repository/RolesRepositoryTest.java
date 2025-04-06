package org.mafisher.togetherbackend.repository;

import org.junit.jupiter.api.Test;
import org.mafisher.togetherbackend.entity.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class RolesRepositoryTest {

    @Autowired
    private RolesRepository rolesRepository;

    @Test
    void whenFindByName_thenReturnRole() {
        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        rolesRepository.save(adminRole);

        Optional<Role> found = rolesRepository.findByName("ADMIN");

        assert found.isPresent();
        assertEquals("ADMIN", found.get().getName());
    }

    @Test
    void whenFindByNameWithNonExistingName_thenReturnEmpty() {
        Optional<Role> found = rolesRepository.findByName("NON_EXISTING");

        assertFalse(found.isPresent());
    }

    @Test
    void whenSaveRoleWithDuplicateName_thenThrowException() {
        Role role1 = new Role();
        role1.setName("USER");
        rolesRepository.save(role1);

        Role role2 = new Role();
        role2.setName("USER");

        assertThrows(DataIntegrityViolationException.class, () -> {
            rolesRepository.save(role2);
        });
    }

    @Test
    void whenSaveRole_thenRoleShouldBePersisted() {
        Role role = new Role();
        role.setName("MODERATOR");

        Role savedRole = rolesRepository.save(role);

        assertNotNull(savedRole.getId());
        assertEquals("MODERATOR", savedRole.getName());

        Optional<Role> foundRole = rolesRepository.findById(savedRole.getId());
        assertTrue(foundRole.isPresent());
        assertEquals(savedRole.getName(), foundRole.get().getName());
    }

    @Test
    void whenDeleteRole_thenRoleShouldBeRemoved() {
        Role role = new Role();
        role.setName("GUEST");
        Role savedRole = rolesRepository.save(role);

        rolesRepository.delete(savedRole);

        Optional<Role> foundRole = rolesRepository.findById(savedRole.getId());
        assertFalse(foundRole.isPresent());
    }
}
