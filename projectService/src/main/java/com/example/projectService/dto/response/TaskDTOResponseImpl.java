package com.example.projectService.dto.response;

import com.example.projectService.enums.TaskStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskDTOResponseImpl implements TaskDTOResponse {
    String id;
    String title;
    String description;
    LocalDate endDate;
    TaskStatus status;
}
