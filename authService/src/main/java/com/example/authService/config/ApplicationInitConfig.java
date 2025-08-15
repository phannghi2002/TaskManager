package com.example.authService.config;

import com.example.authService.entity.Role;
import com.example.authService.entity.User;
import com.example.authService.enums.Permission;
import com.example.authService.repository.PermissionRepository;
import com.example.authService.repository.RoleRepository;
import com.example.authService.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApplicationInitConfig {
    @Value("${app.default-manager.email}")
    String MANAGER_USER_NAME;

    @Value("${app.default-manager.password}")
    String MANAGER_PASSWORD;

    @Bean
    ApplicationRunner applicationRunner(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            // 1. Tạo permission nếu chưa có
            Set<String> existingPermissions = permissionRepository.findAll()
                    .stream()
                    .map(com.example.authService.entity.Permission::getName)
                    .collect(Collectors.toSet());

            List<com.example.authService.entity.Permission> newPermissions = Arrays.stream(Permission.values())
                    .map(Enum::name)
                    .filter(p -> !existingPermissions.contains(p))
                    .map(p -> com.example.authService.entity.Permission.builder().name(p).build())
                    .toList();

            if (!newPermissions.isEmpty()) {
                permissionRepository.saveAll(newPermissions);
                log.info("Created {} new permissions", newPermissions.size());
            }

            for (var entry : DEFAULT_ROLE_PERMISSIONS.entrySet()) {
                com.example.authService.enums.Role roleEnum = entry.getKey();
                Set<Permission> defaultPerms = entry.getValue();

                Role role = roleRepository.findByName(roleEnum.name());
                if (role == null) {
                    // Lấy quyền từ DB dựa trên enum
                    Set<com.example.authService.entity.Permission> rolePermissions = permissionRepository.findAll()
                            .stream()
                            .filter(p -> defaultPerms.contains(Permission.valueOf(p.getName())))
                            .collect(Collectors.toSet());

                    role = Role.builder()
                            .name(roleEnum.name())
                            .permissions(rolePermissions)
                            .build();
                    roleRepository.save(role);
                    log.info("Role {} created with {} permissions", roleEnum.name(), rolePermissions.size());
                }
            }

            // 3. Tạo tài khoản Manager mặc định
            if (userRepository.findByEmail(MANAGER_USER_NAME).isEmpty()) {
                User managerUser = User.builder()
                        .email(MANAGER_USER_NAME)
                        .password(passwordEncoder.encode(MANAGER_PASSWORD))
                        .roles(new HashSet<>(Collections.singletonList(
                                roleRepository.findByName(com.example.authService.enums.Role.MANAGER.name())
                        )))
                        .build();
                userRepository.save(managerUser);
                log.warn("Manager user created with default password: {}", MANAGER_PASSWORD);
            }

            log.info("Application initialization completed.");
        };
    }


    private static final Map<com.example.authService.enums.Role, Set<Permission>> DEFAULT_ROLE_PERMISSIONS = Map.of(
            com.example.authService.enums.Role.MANAGER, EnumSet.allOf(Permission.class),
            com.example.authService.enums.Role.LEADER, EnumSet.of(
                    Permission.MANAGE_PROJECT,
                    Permission.MANAGE_TASK,
                    Permission.ASSIGN_TASK,
                    Permission.VIEW_TASK,
                    Permission.UPDATE_TASK_STATUS,
                    Permission.SUBMIT_TASK
            ),
            com.example.authService.enums.Role.EMPLOYEE, EnumSet.of(
                    Permission.VIEW_TASK,
                    Permission.UPDATE_TASK_STATUS,
                    Permission.SUBMIT_TASK
            )
    );

}
