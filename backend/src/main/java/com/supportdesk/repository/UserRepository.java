package com.supportdesk.repository;

import com.supportdesk.entity.User;
import com.supportdesk.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(Role role);
    boolean existsByAgentId(Integer agentId);
    Optional<User> findByAgentId(Integer agentId);
    long countByRole(Role role);
    long countByRoleIn(java.util.Collection<Role> roles);
}
