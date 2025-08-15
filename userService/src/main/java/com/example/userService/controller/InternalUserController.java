package com.example.userService.controller;

import com.example.userService.dto.request.ProfileCreationRequest;
import com.example.userService.dto.response.ApiResponse;
import com.example.userService.dto.response.UserProfileResponse;
import com.example.userService.service.UserProfileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternalUserController {
    UserProfileService userProfileService;

    @PostMapping("/create-profile")
    ApiResponse<UserProfileResponse> createProfile(@RequestBody ProfileCreationRequest request) {
        UserProfileResponse result = userProfileService.createProfile(request);
        return ApiResponse.<UserProfileResponse>builder()
                .result(result)
                .build();
    }

    @GetMapping("/check-profile/{userId}")
    Boolean checkProfile(@PathVariable String userId){
        return userProfileService.checkProfile(userId);
    }

}
