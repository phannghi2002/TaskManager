package com.example.projectService.dto.response;


import com.example.projectService.enums.TaskStatus;

import java.time.LocalDate;

public interface TaskDTOResponse {
    String getId();
    String getTitle();
    String getDescription();
    LocalDate getEndDate();
    TaskStatus getStatus();
}
