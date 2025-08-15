package com.example.authService.repository;

import com.example.authService.entity.Permission;
import com.example.authService.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String > {
}

