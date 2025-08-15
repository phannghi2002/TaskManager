package com.example.projectService.controller;

import com.example.projectService.dto.response.ApiResponse;
import com.example.projectService.dto.response.ProjectFullResponse;
import com.example.projectService.dto.response.ProjectResponse;
import com.example.projectService.dto.response.TaskResponse;
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
@RequestMapping("/")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectController {
    ProjectService projectService;

    @PostMapping("/create")
    ApiResponse<ProjectResponse> createProject(@RequestBody ProjectCreationRequest request) {
        return ApiResponse.<ProjectResponse>builder()
                .result(projectService.createProject(request))
                .build();
    }

    @PutMapping("/update/{projectId}")
    ApiResponse<ProjectResponse> updateProject(
            @PathVariable String projectId,
            @RequestBody ProjectUpdateRequest request) {
        return ApiResponse.<ProjectResponse>builder()
                .result(projectService.updateProject(projectId, request))
                .build();
    }

    @DeleteMapping("/delete/{projectId}")
    ApiResponse<Void> deleteProject(
            @PathVariable String projectId) {
        projectService.deleteProject(projectId);
        return ApiResponse.<Void>builder()
                .message("Deleted successfully")
                .build();
    }

    @GetMapping("/{projectId}")
    ApiResponse<ProjectFullResponse> getProject(@PathVariable String projectId) {
        return ApiResponse.<ProjectFullResponse>builder()
                .result(projectService.getProject(projectId))
                .build();
    }

    @GetMapping()
    ApiResponse<List<ProjectFullResponse>> getAllProject() {
        return ApiResponse.<List<ProjectFullResponse>>builder()
                .result(projectService.getAllProject())
                .build();
    }

    @PostMapping("/{projectId}/members/{userId}")
    public ApiResponse<Void> addMember(
            @PathVariable String projectId,
            @PathVariable String userId) {

        projectService.addMember(projectId, userId);

        return ApiResponse.<Void>builder()
                .message("Member added successfully")
                .build();
    }


    @DeleteMapping("/{projectId}/members/{userId}")
    public ApiResponse<Void> deleteMember(
            @PathVariable String projectId,
            @PathVariable String userId) {

        projectService.deleteMember(projectId, userId);

        return ApiResponse.<Void>builder()
                .message("Member deleted successfully")
                .build();
    }

//    @GetMapping("/{projectId}/members")
//    public ApiResponse<List<String>> memberInProject(
//            @PathVariable String projectId) {
//        return ApiResponse.<List<String>>builder()
//                .result( projectService.getMemberFromProject(projectId))
//                .build();
//    }

    @PostMapping("/{projectId}/tasks")
    public ApiResponse<Void> addTask(
            @PathVariable String projectId,
            @RequestBody TaskCreationRequest request
    ) {

        projectService.addTask(projectId, request);

        return ApiResponse.<Void>builder()
                .message("Task added successfully")
                .build();
    }

    @DeleteMapping("/{projectId}/task/{taskId}")
    public ApiResponse<Void> deleteTask(
            @PathVariable String projectId,
            @PathVariable String taskId) {

        projectService.deleteTask(projectId, taskId);

        return ApiResponse.<Void>builder()
                .message("Task deleted successfully")
                .build();
    }

    @PutMapping("/{projectId}/task/{taskId}")
    public ApiResponse<TaskResponse> updateTask(
            @PathVariable String projectId,
            @PathVariable String taskId,
            @RequestBody TaskUpdateRequest request
            ) {
        return ApiResponse.<TaskResponse>builder()
                .result( projectService.updateTask(projectId, taskId, request))
                .build();
    }

}
