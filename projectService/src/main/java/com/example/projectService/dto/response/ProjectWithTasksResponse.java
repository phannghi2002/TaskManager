package com.example.projectService.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectWithTasksResponse {
    String id;
    String name;
    String description;

    List<TaskDTOResponseImpl> tasks;
}
