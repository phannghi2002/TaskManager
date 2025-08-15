package com.example.authService.controller;

import com.example.authService.dto.request.AuthenticationRequest;

import com.example.authService.dto.request.UserCreationRequest;
import com.example.authService.dto.response.ApiResponse;
import com.example.authService.dto.response.AuthenticationResponse;

import com.example.authService.dto.response.UserResponse;
import com.example.authService.service.AuthenticationService;
import com.example.authService.service.UserService;
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
    ApiResponse<UserResponse> createUser(@RequestBody UserCreationRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(request))
                .build();
    }

}
