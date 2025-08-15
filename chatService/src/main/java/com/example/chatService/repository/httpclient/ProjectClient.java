package com.example.chatService.repository.httpclient;

import com.example.chatService.dto.response.ApiResponse;
import com.example.chatService.dto.response.ProjectMemberInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name="project-service", url="${app.services.project}")
public interface ProjectClient {
    @GetMapping(value = "/internal/{projectId}/members", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<ProjectMemberInfoResponse> getMemberInProject(@PathVariable String projectId);
}
