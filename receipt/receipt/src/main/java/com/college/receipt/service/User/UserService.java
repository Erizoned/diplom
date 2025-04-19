package com.college.receipt.service.User;

import com.college.receipt.entities.Role;
import com.college.receipt.entities.User;
import com.college.receipt.repositories.RoleRepository;
import com.college.receipt.DTO.UserDto;
import com.college.receipt.exceptions.UserAlreadyExistException;
import com.college.receipt.repositories.UserRepository;
import com.college.receipt.service.JWTService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Transactional
public class UserService {

    public static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JWTService jwtService;

    public Optional<User> findUserById(Long id){
        return userRepository.findById(id);
    }

    private Set<Role> getRolesFromNames(List<String> roleNames) {
        return new HashSet<>(roleRepository.findByNameIn(roleNames));
    }

    public User registerNewUserAccount(UserDto userDto) throws UserAlreadyExistException {
        if (emailExists(userDto.getEmail())) {
            throw new UserAlreadyExistException("There is an account with that email address: "
                    + userDto.getEmail());
        }

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setEmail(userDto.getEmail());
        user.setRoles(getRolesFromNames(Arrays.asList("USER")));
        logger.info("Пользователь {} успешно зарегистрировался c ролью {}", user.getUsername(), user.getRoles());

        return userRepository.save(user);
    }

    public String verify(User user) throws AuthenticationException {
        try {
            Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));

            if (authentication.isAuthenticated()) {
                return jwtService.generateToken(user.getEmail());
            }
        }catch (AuthenticationException e){
            throw new RuntimeException("Ошибка. Пользователь не вошёл в аккаунт: " + e.getMessage());
        }
        throw new RuntimeException("Ошибка. Пользователь не вошёл в аккаунт");
    }

    public User findAuthenticatedUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated()){
            return userRepository.findByEmail(authentication.getName());
        }
        else {
            throw new RuntimeException("Пользователь не вошёл в аккаунт");
        }
    }

    private boolean emailExists(String email) {
        return userRepository.findByEmail(email) != null;
    }
}
