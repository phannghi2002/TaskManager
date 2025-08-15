package com.example.projectService.dto.resquest;

import com.example.projectService.enums.ProjectStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectUpdateRequest {
    String name;
    String description;
    LocalDate startDate;
    LocalDate endDate;
    ProjectStatus status;
}
