package com.example.projectService.repository.httpclient;

import com.example.projectService.dto.response.ApiResponse;
import com.example.projectService.dto.response.NotMemberResponse;
import com.example.projectService.dto.resquest.NotMemberRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name="profile-service", url="${app.services.profile}")
public interface ProfileClient {
    @GetMapping(value = "/internal/check-profile/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    Boolean checkProfile(@PathVariable String userId);
    
    @PostMapping(value = "/internal/get-not-member", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<List<NotMemberResponse>> getNotMember(@RequestBody NotMemberRequest request);

}
