package com.example.projectService.dto.resquest;

import com.example.projectService.enums.TaskStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskUpdateRequest {
    String title;
    String description;
    String assigneeId;
    LocalDate startDate;
    LocalDate endDate;
    TaskStatus status;
}
