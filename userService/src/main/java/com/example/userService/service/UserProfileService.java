package com.example.userService.service;

import com.example.userService.dto.request.ProfileCreationRequest;
import com.example.userService.dto.request.UpdateUserProfileRequest;
import com.example.userService.dto.response.ApiResponse;
import com.example.userService.dto.response.NotMemberResponse;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.time.LocalDateTime;
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

            Object roleObj = event.get("role");
            String role;
            if (roleObj instanceof List) {
                List<?> roles = (List<?>) roleObj;
                role = roles.get(0).toString(); // hoặc join lại thành String
            } else {
                role = roleObj.toString();
            }


            ProfileCreationRequest request = ProfileCreationRequest.builder()
                    .userId(userId)
                    .email((String) event.get("email"))
                    .fullName((String) event.get("fullName"))
//                    .dob(LocalDate.parse((String) event.get("lastName")))
                    .dob(dob)
                    .city((String) event.get("city"))
                    .role((String) event.get("role"))
                    .build();
            System.out.println("Received request: " + request);

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
                .email(request.getEmail())
                .fullName(request.getFullName())
                .dob(request.getDob())
                .joinDate(LocalDateTime.now())
                .city(request.getCity())
                .role(request.getRole())
                .build();
        userProfile = userProfileRepository.save(userProfile);

        UserProfileResponse userProfileResponse = UserProfileResponse.builder()
                .userId(userProfile.getUserId())
                .email(userProfile.getEmail())
                .fullName(userProfile.getFullName())
                .dob(userProfile.getDob())
                .joinDate(userProfile.getJoinDate())
                .city(userProfile.getCity())
                .role(userProfile.getRole())
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
    public String getUserIdFromToken() {
        var context = SecurityContextHolder.getContext();

        var authentication = context.getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();

            String subject = jwt.getSubject();
            Map<String, Object> claims = jwt.getClaims(); // All claims

            return (String) claims.get("userId");
        } else {
            throw new IllegalStateException("No JWT token found in SecurityContext");
        }
    }


    public ApiResponse<UserProfile> getProfileMySelf() {
        String userId = getUserIdFromToken();
        UserProfile userProfile = userProfileRepository.findByUserId(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return ApiResponse.<UserProfile>builder()
                .result(userProfile)
                .build();
    }

    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<UserProfile> updateUserProfile(String userId, UpdateUserProfileRequest request) {
        UserProfile userProfile = userProfileRepository.findByUserId(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userProfile.setFullName(request.getFullName());
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


        userProfile.setFullName(request.getFullName() != null ? request.getFullName() : userProfile.getFullName());
        userProfile.setDob(request.getDob() != null ? request.getDob() : userProfile.getDob());
        userProfile.setCity(request.getCity() != null ? request.getCity() : userProfile.getCity());


        Map<String, Object> changedEventFields = new HashMap<>();

        if (request.getEmail() != null && !request.getEmail().equals(userProfile.getEmail())) {
            userProfile.setEmail(request.getEmail());
            changedEventFields.put("email", request.getEmail());
        }

        // Check for role change
        if (request.getRole() != null && !request.getRole().equals(userProfile.getRole())) {
            userProfile.setRole(request.getRole());
            changedEventFields.put("role", request.getRole());
        }

        if (!changedEventFields.isEmpty()) {
            // Add the userId which is mandatory
            changedEventFields.put("userId", userId);
            sendEventUpdateUserInAuthService(changedEventFields);
        }

        userProfileRepository.save(userProfile);

        return ApiResponse.<UserProfile>builder()
                .result(userProfile)
                .build();
    }

    public void sendEventUpdateUserInAuthService(Map<String, Object> event) {
        kafkaTemplate.send("update-user-events", event);
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

    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<List<UserProfile>> searchProfiles(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {

            return  ApiResponse.<List<UserProfile>>builder()
                    .result(userProfileRepository.findAll())
                    .build();
        }
        return ApiResponse.<List<UserProfile>>builder()
                .result(userProfileRepository.searchProfilesByKeyword(keyword))
                .build();
    }

//    public ApiResponse<List<UserProfile>> getMemberNotInProject(List<String> listIds){
//        return ApiResponse.<List<UserProfile>>builder()
//                .result(userProfileRepository.getUserNotMemberInProject(listIds))
//                .build();
//    }

    public ApiResponse<List<NotMemberResponse>> getMemberNotInProject(List<String> listIds){
        return ApiResponse.<List<NotMemberResponse>>builder()
                .result(userProfileRepository.getUserNotMemberInProject(listIds))
                .build();
    }

//    @KafkaListener(groupId = "project-group", topics = "listId-member-events")
//    public void listenReturnMemberNotInProjectEvents(@Payload Map<String, Object> event) {
//        System.out.println("Received event: " + event);
//
//        List<String> memberIds = (List<String>) event.get("listIds");
//
//        try {
//            List<UserProfile> notMembers = userProfileRepository.getUserNotMemberInProject(memberIds);
//
//        } catch (Exception e) {
//
//            sendEventDeleteUserInAuthService(userId);
//            System.err.println("Failed to create profile, rollback user: " + e.getMessage());
//        }
//
//    }
//
//    public void sendInfoUserNotMember(Map<String, Object> event) {
//
//        kafkaTemplate.send("listId-member-events", event);
//    }




}

