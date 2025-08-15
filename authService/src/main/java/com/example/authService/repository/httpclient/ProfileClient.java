package com.example.authService.repository.httpclient;

import com.example.authService.dto.request.ProfileCreationRequest;
import com.example.authService.dto.response.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="profile-service", url="${app.services.profile}")
public interface ProfileClient {
    @PostMapping(value = "/internal/create-profile", produces = MediaType.APPLICATION_JSON_VALUE)
    UserProfileResponse createProfile(@RequestBody ProfileCreationRequest request);
}
