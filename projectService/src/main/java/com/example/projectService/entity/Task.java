package com.example.projectService.entity;

import com.example.projectService.enums.ProjectStatus;
import com.example.projectService.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

     String title;
     String description;

    @Enumerated(EnumType.STRING)
    TaskStatus status;

    LocalDate startDate;
    LocalDate endDate;

    String createBy;
    String assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    Project project;
}
