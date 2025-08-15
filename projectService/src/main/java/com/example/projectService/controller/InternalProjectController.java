package com.example.projectService.controller;

import com.example.projectService.dto.response.*;
import com.example.projectService.dto.resquest.ProjectCreationRequest;
import com.example.projectService.dto.resquest.ProjectUpdateRequest;
import com.example.projectService.dto.resquest.TaskCreationRequest;
import com.example.projectService.dto.resquest.TaskUpdateRequest;
import com.example.projectService.service.ProjectService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternalProjectController {
    ProjectService projectService;

    @GetMapping("/{projectId}/members")
    public ApiResponse<ProjectMemberInfoResponse> memberInProject(
            @PathVariable String projectId) {
        return ApiResponse.<ProjectMemberInfoResponse>builder()
                .result( projectService.getMemberFromProject(projectId))
                .build();
    }


}
