package com.college.receipt.service;

import com.college.receipt.entities.Role;
import com.college.receipt.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import com.college.receipt.repositories.RoleRepository;
import com.college.receipt.repositories.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Validated
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public User registerUser(User user) {
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Role Not Found"));
        user.getRoles().add(userRole);
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User deleteUserById(Long id) {
        User user = findUserById(id).orElseThrow(() -> new NoSuchElementException("User not found with id: " + id));
        userRepository.delete(user);
        return user;
    }

    @Override
    public List<User> findAllUsers() {
        return (List<User>) userRepository.findAll();
    }

    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public void assignRoleToUser(String username, String roleName) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found with username: " + username));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new NoSuchElementException("Role not found with name: " + roleName));

        user.getRoles().add(role);
        userRepository.save(user);
    }
}
