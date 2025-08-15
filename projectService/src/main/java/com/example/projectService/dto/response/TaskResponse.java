package com.example.projectService.dto.response;

import com.example.projectService.enums.TaskStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskResponse {
    String id;
     String title;
     String description;
     TaskStatus status;
     String assigneeId;
     LocalDate deadline;
}
