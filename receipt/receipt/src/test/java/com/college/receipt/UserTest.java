package com.college.receipt;

import com.college.receipt.entities.User;
import com.college.receipt.DTO.UserDto;
import com.college.receipt.repositories.UserRepository;
import com.college.receipt.service.User.UserService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import com.college.receipt.entities.Role;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;


    @Test
    public void UserService_UserRegistration_User() throws IOException {

        UserDto userDto = UserDto.builder()
                .username("bomboclac")
                .email("bomboclac@gmail.com")
                .password("123")
                .build();

        User savedUser = userService.registerNewUserAccount(userDto);

        Set<String> roleNames = savedUser.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        assertNotNull(savedUser);
        assertEquals(1, roleNames.size());
        assertTrue(roleNames.contains("USER"));
    }
}
