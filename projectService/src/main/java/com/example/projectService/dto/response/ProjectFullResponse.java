package com.example.projectService.dto.response;

import com.example.projectService.enums.ProjectStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectFullResponse {
    String id;
    String name;
    String description;

    LocalDate startDate;
    LocalDate endDate;
    String createBy;

    ProjectStatus status;
    List<MemberResponse> members;
    List<TaskResponse> tasks;

}
