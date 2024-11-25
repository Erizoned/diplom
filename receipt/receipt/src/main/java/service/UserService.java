package service;

import com.college.receipt.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User registerUser(User user);
    Optional<User> findUserById(Long id);
    User deleteUserById(Long id);
    List<User> findAllUsers();
    User updateUser(User user);
    void assignRoleToUser(String username, String roleName);
}
