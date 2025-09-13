package com.example.authService.service;

import com.example.authService.dto.request.ChangePasswordRequest;
import com.example.authService.dto.request.UserCreationRequest;
import com.example.authService.dto.response.RoleResponse;
import com.example.authService.dto.response.UserResponse;
import com.example.authService.entity.Permission;
import com.example.authService.entity.Role;
import com.example.authService.entity.User;
import com.example.authService.exception.AppException;
import com.example.authService.exception.ErrorCode;
import com.example.authService.repository.RoleRepository;
import com.example.authService.repository.UserRepository;
import com.example.authService.repository.httpclient.ProfileClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;

    RoleRepository roleRepository;

    PasswordEncoder passwordEncoder;

    ProfileClient profileClient;

    KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

//    @PreAuthorize("hasRole('MANAGER')")
    public UserResponse createUser(UserCreationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) throw new AppException(ErrorCode.USER_EXISTED);

//        Set<Role> roles = roleRepository.findByNameIn(request.getRoles());
//
//        if (roles.size() != request.getRoles().size())
//            throw new AppException(ErrorCode.ROLE_NOT_FOUND);

        Set<Role> roles;
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            Role defaultRole = roleRepository.findByName(com.example.authService.enums.Role.EMPLOYEE.name());
            roles = new HashSet<>();
            roles.add(defaultRole);
        } else {
            roles = roleRepository.findByNameIn(request.getRoles());
            if (roles.size() != request.getRoles().size()) {
                throw new AppException(ErrorCode.ROLE_NOT_FOUND);
            }
        }



        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);

        //HTTP
//        ProfileCreationRequest profileRequest = ProfileCreationRequest.builder()
//                .userId(savedUser.getId())
//                .firstName(request.getFirstName())
//                .lastName(request.getLastName())
//                .dob(request.getDob())
//                .city(request.getCity())
//                .build();
//
//        profileClient.createProfile(profileRequest);

        //KAFKA
        Map<String, Object> event = new HashMap<>();
        event.put("userId", savedUser.getId());
        event.put("email", savedUser.getEmail());
        event.put("fullName", request.getFullName());
        event.put("dob", request.getDob());
        event.put("city", request.getCity());


        Set<String> roleNames = savedUser.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        List<String> roleList = new ArrayList<>(roleNames);

        String roleString = null;
        if (!roleList.isEmpty()) {

            roleString = roleList.get(0);
        }

        event.put("role", roleString);

        System.out.println("Sending event: " + event);
        kafkaTemplate.send("create-user-profile-events", event);

        Set<RoleResponse> roleResponses = savedUser.getRoles().stream()
                .map(role -> {
                    Set<String> permissionResponses = role.getPermissions().stream()
                            .map(Permission::getName)
                            .collect(Collectors.toSet());

                    return RoleResponse.builder()
                            .name(role.getName())
                            .permissions(permissionResponses)
                            .build();
                })
                .collect(Collectors.toSet());


        return UserResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .roles(roleResponses)
                .build();
    }

    @KafkaListener(groupId = "auth-group", topics = "delete-user-events")
    public void listenDeleteUserProfileEvents(@Payload Map<String, Object> event) {
        System.out.println("Received event: " + event);

        String id = ((String) event.get("userId"));

        userRepository.deleteById(id);

    }

    @KafkaListener(groupId = "auth-group", topics = "update-user-events")
    public void listenUpdateUserProfileEvents(@Payload Map<String, Object> event) {
        System.out.println("Received event: " + event);

        String userId = (String) event.get("userId");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Check if the event contains an 'email' field and update it
        if (event.containsKey("email") && event.get("email") instanceof String) {
            String newEmail = (String) event.get("email");
            user.setEmail(newEmail);
            System.out.println("Email updated for user ID: " + userId + " to " + newEmail);
        }

        // Check if the event contains a 'role' field and update it
        if (event.containsKey("role") && event.get("role") instanceof String) {
            String roleName = (String) event.get("role");

            // Find the Role entity from the database
            Role role = roleRepository.findByName(roleName);

            if (role == null) {
                throw new AppException(ErrorCode.ROLE_NOT_FOUND);
            }

            // Create a new Set<Role> with the updated role and set it
            Set<Role> newRoles = new HashSet<>();
            newRoles.add(role);
            user.setRoles(newRoles);
            System.out.println("Role updated for user ID: " + userId + " to " + roleName);
        }

        // Save the user with the updated fields
        userRepository.save(user);
        System.out.println("User profile updated successfully.");
    }

    public String updatePassword(ChangePasswordRequest request){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.WRONG_PASSWORD);
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.NEW_PASSWORD_SAME_AS_OLD);
        }

        if (!request.getNewPassword().equals(request.getRepeatNewPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_SAME);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return "Password changed successfully";
    }

}
