package com.example.projectService.dto.resquest;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskCreationRequest {
    @NotBlank(message = "Title is mandatory")
    String title;

    @NotBlank(message = "Description is mandatory")
    String description;

    @NotBlank(message = "AssigneeId is mandatory")
    String assigneeId;

    @NotBlank(message = "Start date is mandatory")
    LocalDate startDate;

    @NotBlank(message = "End date is mandatory")
    LocalDate endDate;

}
