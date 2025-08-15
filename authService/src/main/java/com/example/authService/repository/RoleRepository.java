package com.example.authService.repository;

import com.example.authService.entity.Role;
import com.example.authService.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, String > {
    Role findByName(String name);

    Set<Role> findByNameIn(Set<String> names);
}

