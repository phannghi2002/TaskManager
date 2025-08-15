package com.example.projectService.dto.response;

import com.example.projectService.enums.ProjectStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectResponse {
    String id;
    String name;
    String description;

    LocalDate startDate;
    LocalDate endDate;
    String createBy;
    ProjectStatus status;
}
