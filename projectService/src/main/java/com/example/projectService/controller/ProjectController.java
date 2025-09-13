package com.example.projectService.controller;

import com.example.projectService.dto.response.*;
import com.example.projectService.dto.resquest.*;
import com.example.projectService.service.ProjectService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectController {
    ProjectService projectService;

    @PreAuthorize("hasAnyRole('MANAGER','LEADER')")
    @PostMapping("/create")
    ApiResponse<ProjectResponse> createProject(@Valid @RequestBody ProjectCreationRequest request) {
        return ApiResponse.<ProjectResponse>builder()
                .result(projectService.createProject(request))
                .build();
    }

    @PreAuthorize("hasAnyRole('MANAGER','LEADER')")
    @PutMapping("/update/{projectId}")
    ApiResponse<ProjectResponse> updateProject(
            @Valid
            @PathVariable String projectId,
            @RequestBody ProjectUpdateRequest request) {
        return ApiResponse.<ProjectResponse>builder()
                .result(projectService.updateProject(projectId, request))
                .build();
    }

    @PreAuthorize("hasAnyRole('MANAGER','LEADER')")
    @DeleteMapping("/delete/{projectId}")
    ApiResponse<Void> deleteProject(@PathVariable String projectId) {
        projectService.deleteProject(projectId);
        return ApiResponse.<Void>builder()
                .message("Deleted successfully")
                .build();
    }

    @PreAuthorize("hasAnyRole('MANAGER','LEADER')")
    @GetMapping("/{projectId}")
    ApiResponse<ProjectFullResponse> getProject(@PathVariable String projectId) {
        return ApiResponse.<ProjectFullResponse>builder()
                .result(projectService.getProject(projectId))
                .build();
    }

    @PreAuthorize("hasAnyRole('MANAGER','LEADER')")
    @GetMapping("/get-all-project")
    ApiResponse<List<ProjectFullResponse>> getAllProject() {
        return ApiResponse.<List<ProjectFullResponse>>builder()
                .result(projectService.getAllProject())
                .build();
    }

    @PreAuthorize("hasAnyRole('MANAGER','LEADER')")
    @PostMapping("/{projectId}/members/{userId}")
    public ApiResponse<Void> addMember(@PathVariable String projectId,
                                       @PathVariable String userId) {
        projectService.addMember(projectId, userId);

        return ApiResponse.<Void>builder()
                .message("Member added successfully")
                .build();
    }

    @PreAuthorize("hasAnyRole('MANAGER','LEADER')")
    @PostMapping("/{projectId}/members")
    public ApiResponse<Void> addMember(@PathVariable String projectId,
                                       @RequestBody List<String> userIds) {
        projectService.addMembers(projectId, userIds);

        return ApiResponse.<Void>builder()
                .message("Member list added successfully")
                .build();
    }

    @PreAuthorize("hasAnyRole('MANAGER','LEADER')")
    @DeleteMapping("/{projectId}/members/{userId}")
    public ApiResponse<Void> deleteMember(
            @PathVariable String projectId,
            @PathVariable String userId) {

        projectService.deleteMember(projectId, userId);

        return ApiResponse.<Void>builder()
                .message("Member deleted successfully")
                .build();
    }

    @GetMapping("/{projectId}/members")
    public ApiResponse<List<String>> memberInProject(
            @PathVariable String projectId) {
        return ApiResponse.<List<String>>builder()
                .result(projectService.getListIdMemberFromProject(projectId))
                .build();
    }

    @PostMapping("/{projectId}/tasks")
    public ApiResponse<Void> addTask(
            @Valid
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
                .result(projectService.updateTask(projectId, taskId, request))
                .build();
    }


    @PreAuthorize("#userId == authentication.principal.claims['userId']")
    @GetMapping("/{userId}/projects-with-tasks")
    ApiResponse<List<ProjectWithTasksResponse>> getProjectsWithUserTasks(@PathVariable String userId) {
        return ApiResponse.<List<ProjectWithTasksResponse>>builder()
                .result(projectService.getProjectsWithUserTasks(userId))
                .build();
    }

    @GetMapping("/projects-with-my-tasks")
    ApiResponse<List<ProjectWithTasksResponse>> getProjectsWithMyTasks() {
        return ApiResponse.<List<ProjectWithTasksResponse>>builder()
                .result(projectService.getProjectsWithMyTasks())
                .build();
    }

    @PreAuthorize("#userId == authentication.principal.claims['userId']")
    @GetMapping("/api/auth/details/{userId}")
    public String getAuthenticationDetails(@PathVariable String userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return "No authenticated user found.";
        }

        // In toàn bộ đối tượng Authentication để xem các thuộc tính
        System.out.println("Authentication Object: " + authentication);

        // In các thuộc tính riêng lẻ
        System.out.println("Principal: " + authentication.getPrincipal());

        System.out.println("PrincipalID: " + authentication.getPrincipal());

        System.out.println("Credentials: " + authentication.getCredentials());
        System.out.println("Authorities: " + authentication.getAuthorities());
        System.out.println("Details: " + authentication.getDetails());
        System.out.println("Name: " + authentication.getName());

        // Bạn có thể cast principal để truy cập các field tùy chỉnh
        // Ví dụ: UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        return "Authentication details have been logged.";
    }

    @PreAuthorize("hasAnyRole('MANAGER','LEADER')")
    @PostMapping("/get-not-member-in-project")
    ApiResponse<List<NotMemberResponse>> getNotMemberInProject(@RequestBody NotMemberRequest request) {
        return projectService.getNotMemberInProject(request);
    }

}
