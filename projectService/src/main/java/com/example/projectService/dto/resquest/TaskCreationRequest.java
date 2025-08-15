package com.example.projectService.dto.resquest;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskCreationRequest {
    String title;
    String description;
    String assigneeId;
    LocalDate startDate;
    LocalDate endDate;

}
