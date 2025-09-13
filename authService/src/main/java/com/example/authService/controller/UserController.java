package com.example.authService.controller;

import com.example.authService.dto.request.ChangePasswordRequest;
import com.example.authService.dto.request.UserCreationRequest;
import com.example.authService.dto.response.ApiResponse;
import com.example.authService.dto.response.UserResponse;
import com.example.authService.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @PostMapping("/registration")
    ApiResponse<UserResponse> createUser(@Valid @RequestBody UserCreationRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(request))
                .build();
    }

    @PostMapping("/change-password")
    ApiResponse<?> updatePassword(@Valid @RequestBody ChangePasswordRequest request) {
        return ApiResponse.<UserResponse>builder()
                .message(userService.updatePassword(request))
                .build();
    }

}
