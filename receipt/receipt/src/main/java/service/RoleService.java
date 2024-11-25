package service;

import com.college.receipt.Role;

import java.util.List;
import java.util.Optional;

public interface RoleService {
    Role createRole(Role role);
    Optional<Role> getRoleById(Long id);
    Role updateRole(Long id, Role role);
    void deleteRole(Long id);
    List<Role> getAllRoles();
}
