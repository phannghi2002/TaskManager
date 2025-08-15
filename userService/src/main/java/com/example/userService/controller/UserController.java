package com.example.userService.controller;

import com.example.userService.dto.request.ProfileCreationRequest;
import com.example.userService.dto.request.UpdateUserProfileRequest;
import com.example.userService.dto.response.ApiResponse;
import com.example.userService.dto.response.UserProfileResponse;
import com.example.userService.entity.UserProfile;
import com.example.userService.service.UserProfileService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserProfileService userProfileService;

    @GetMapping("/get-all-profile")
    ApiResponse<List<UserProfile>> getAllProfile() {
        return userProfileService.getAllProfile();
    }

    @GetMapping("/get-single-user-profile/{userId}")
    ApiResponse<UserProfile> getSingleProfile(@PathVariable String userId) {
        return userProfileService.getSingleProfile(userId);
    }

    @PutMapping("/update/{userId}")
    ApiResponse<UserProfile> updateUserProfile(@PathVariable String userId,
                                               @RequestBody UpdateUserProfileRequest request) {
        return userProfileService.updateUserProfile(userId, request);
    }

    @PatchMapping("/update/{userId}")
    ApiResponse<UserProfile> updatePartUserProfile(@PathVariable String userId,
                                                   @RequestBody UpdateUserProfileRequest request) {
        return userProfileService.updatePartUserProfile(userId, request);
    }

    @DeleteMapping("/delete/{userId}")
    ApiResponse<?> deleteUserProfile(@PathVariable String userId) {
        userProfileService.deleteUser(userId);
        return ApiResponse.builder()
                .message("Delete user success")
                .build();
    }


}
