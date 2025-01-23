package com.college.receipt.service.User;

import com.college.receipt.entities.Role;
import com.college.receipt.repositories.RoleRepository;
import com.college.receipt.entities.User;
import com.college.receipt.entities.UserDto;
import com.college.receipt.exceptions.UserAlreadyExistException;
import com.college.receipt.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService implements IUserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    public Optional<User> findUserById(Long id){
        return userRepository.findById(id);
    }

    private Set<Role> getRolesFromNames(List<String> roleNames) {
        return roleRepository.findByNameIn(roleNames).stream().collect(Collectors.toSet());
    }

    @Override
    public User registerNewUserAccount(UserDto userDto) throws UserAlreadyExistException {
        if (emailExists(userDto.getEmail())) {
            throw new UserAlreadyExistException("There is an account with that email address: "
                    + userDto.getEmail());
        }

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setEmail(userDto.getEmail());
        user.setRoles(getRolesFromNames(Arrays.asList("ROLE_USER")));

        return userRepository.save(user);
    }

    private boolean emailExists(String email) {
        return userRepository.findByEmail(email) != null;
    }
}
