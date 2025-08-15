package com.example.authService.service;

import com.example.authService.dto.request.ProfileCreationRequest;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    @PreAuthorize("hasRole('MANAGER')")
    public UserResponse createUser(UserCreationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) throw new AppException(ErrorCode.USER_EXISTED);

        Set<Role> roles = roleRepository.findByNameIn(request.getRoles());

        if (roles.size() != request.getRoles().size())
            throw new AppException(ErrorCode.ROLE_NOT_FOUND);

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
        event.put("firstName", request.getFirstName());
        event.put("lastName", request.getLastName());
        event.put("dob", request.getDob());
        event.put("city", request.getCity());

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

}
