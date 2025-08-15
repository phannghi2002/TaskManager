package com.example.userService.service;

import com.example.userService.dto.request.ProfileCreationRequest;
import com.example.userService.dto.request.UpdateUserProfileRequest;
import com.example.userService.dto.response.ApiResponse;
import com.example.userService.dto.response.UserProfileResponse;
import com.example.userService.entity.UserProfile;
import com.example.userService.exception.AppException;
import com.example.userService.exception.ErrorCode;
import com.example.userService.repository.UserProfileRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserProfileService {
    UserProfileRepository userProfileRepository;

    KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    @KafkaListener(groupId = "user-group", topics = "create-user-profile-events")
    public void listenCreateUserProfileEvents(@Payload Map<String, Object> event) {
        System.out.println("Received event: " + event);

        String userId = ((String) event.get("userId"));

        try {
            Object dobObj = event.get("dob");
            LocalDate dob;
            if (dobObj instanceof List) {
                List<?> dobList = (List<?>) dobObj;
                dob = LocalDate.of(
                        ((Number) dobList.get(0)).intValue(),
                        ((Number) dobList.get(1)).intValue(),
                        ((Number) dobList.get(2)).intValue()
                );
            } else {
                dob = LocalDate.parse(dobObj.toString());
            }

            ProfileCreationRequest request = ProfileCreationRequest.builder()
                    .userId(userId)
                    .firstName((String) event.get("firstName"))
                    .lastName((String) event.get("lastName"))
//                    .dob(LocalDate.parse((String) event.get("lastName")))
                    .dob(dob)
                    .city((String) event.get("city"))
                    .build();

            createProfile(request);

        } catch (Exception e) {

            sendEventDeleteUserInAuthService(userId);
            System.err.println("Failed to create profile, rollback user: " + e.getMessage());
        }

    }

    public void sendEventDeleteUserInAuthService(String userId) {
        Map<String, Object> event = new HashMap<>();
        event.put("userId", userId);

        kafkaTemplate.send("delete-user-events", event);
    }

    public UserProfileResponse createProfile(ProfileCreationRequest request) {
        if (userProfileRepository.existsByUserId(request.getUserId()))
            throw new AppException(ErrorCode.USER_EXISTED);

        UserProfile userProfile = UserProfile.builder()
                .userId(request.getUserId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dob(request.getDob())
                .city(request.getCity())
                .build();
        userProfile = userProfileRepository.save(userProfile);

        UserProfileResponse userProfileResponse = UserProfileResponse.builder()
                .userId(userProfile.getUserId())
                .firstName(userProfile.getFirstName())
                .lastName(userProfile.getLastName())
                .dob(userProfile.getDob())
                .city(userProfile.getCity())
                .build();

        return userProfileResponse;
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'LEADER')")
    public ApiResponse<List<UserProfile>> getAllProfile() {
        List<UserProfile> getAll = userProfileRepository.findAll();

        return ApiResponse.<List<UserProfile>>builder()
                .result(getAll)
                .build();
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'LEADER')")
    public ApiResponse<UserProfile> getSingleProfile(String userId) {
        UserProfile userProfile = userProfileRepository.findByUserId(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return ApiResponse.<UserProfile>builder()
                .result(userProfile)
                .build();
    }

    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<UserProfile> updateUserProfile(String userId, UpdateUserProfileRequest request) {
        UserProfile userProfile = userProfileRepository.findByUserId(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userProfile.setFirstName(request.getFirstName());
        userProfile.setLastName(request.getLastName());
        userProfile.setDob(request.getDob());
        userProfile.setCity(request.getCity());

        userProfileRepository.save(userProfile);

        return ApiResponse.<UserProfile>builder()
                .result(userProfile)
                .build();
    }

    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<UserProfile> updatePartUserProfile(String userId, UpdateUserProfileRequest request) {
        UserProfile userProfile = userProfileRepository.findByUserId(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userProfile.setFirstName(request.getFirstName() != null ? request.getFirstName() : userProfile.getFirstName());
        userProfile.setLastName(request.getLastName() != null ? request.getLastName() : userProfile.getLastName());
        userProfile.setDob(request.getDob() != null ? request.getDob() : userProfile.getDob());
        userProfile.setCity(request.getCity() != null ? request.getCity() : userProfile.getCity());

        userProfileRepository.save(userProfile);

        return ApiResponse.<UserProfile>builder()
                .result(userProfile)
                .build();
    }


    @PreAuthorize("hasRole('MANAGER')")
    @Transactional
    public void deleteUser(String userId) {
        if (!userProfileRepository.existsByUserId(userId)) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        userProfileRepository.deleteByUserId(userId);

        sendEventDeleteUserInAuthService(userId);
    }

    public Boolean checkProfile(String userId) {
        return userProfileRepository.existsByUserId(userId);
    }
}

