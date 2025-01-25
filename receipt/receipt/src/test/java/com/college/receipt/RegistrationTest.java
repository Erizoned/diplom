package com.college.receipt;

import com.college.receipt.controllers.RegistrationController;
import com.college.receipt.entities.Role;
import com.college.receipt.entities.User;
import com.college.receipt.entities.UserDto;
import com.college.receipt.service.User.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RegistrationTest {

    @Autowired
    private UserService userService;

    @Test
    public void testUserRoleAssignment() {
        User user = new User();
        user.setUsername("testUser");
        user.setEmail("testUser@example.com");

        Role userRole = new Role("ROLE_USER");
        Role adminRole = new Role("ROLE_ADMIN");

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        roles.add(adminRole);

        user.setRoles(roles);

        assertNotNull(user);

        assertEquals("testUser", user.getUsername());
        assertEquals("testUser@example.com", user.getEmail());

        assertNotNull(user.getRoles());
        assertEquals(2, user.getRoles().size());
        assertTrue(user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_USER")));
        assertTrue(user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN")));
    }

}
