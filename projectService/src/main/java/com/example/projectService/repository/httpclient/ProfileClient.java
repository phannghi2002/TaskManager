package com.example.projectService.repository.httpclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="profile-service", url="${app.services.profile}")
public interface ProfileClient {
    @GetMapping(value = "/internal/check-profile/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    Boolean checkProfile(@PathVariable String userId);
}
