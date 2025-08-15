package com.example.projectService.mapper;

import com.example.projectService.dto.response.MemberResponse;
import com.example.projectService.dto.response.ProjectFullResponse;
import com.example.projectService.dto.response.TaskResponse;
import com.example.projectService.entity.Project;
import com.example.projectService.entity.ProjectMember;
import com.example.projectService.entity.Task;

import java.util.Collections;
import java.util.stream.Collectors;

public class ProjectMapper {

    public static ProjectFullResponse toFullResponse(Project project) {
        ProjectFullResponse response = ProjectFullResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .createBy(project.getCreateBy())
                .status(project.getStatus())
                .build();

        // Map members
        if (project.getMembers() != null) {
            response.setMembers(
                    project.getMembers().stream()
                            .map(ProjectMapper::toMemberResponse)
                            .collect(Collectors.toList())
            );
        } else {
            response.setMembers(Collections.emptyList());
        }

        // Map tasks
        if (project.getTasks() != null) {
            response.setTasks(
                    project.getTasks().stream()
                            .map(ProjectMapper::toTaskResponse)
                            .collect(Collectors.toList())
            );
        } else {
            response.setTasks(Collections.emptyList());
        }

        return response;
    }

    private static MemberResponse toMemberResponse(ProjectMember member) {
        return MemberResponse.builder()
                .id(member.getId().getUserId())
                .joinedDate(member.getJoinedDate())
                .build();
    }


    private static TaskResponse toTaskResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .assigneeId(task.getAssignedTo())
                .deadline(task.getEndDate())
                .build();

    }
}

