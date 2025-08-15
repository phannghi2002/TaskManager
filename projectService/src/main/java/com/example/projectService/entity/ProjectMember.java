package com.example.projectService.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "project_members")
public class ProjectMember {
    @EmbeddedId
    ProjectMemberId id; // gồm projectId + userId

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("projectId") // Ánh xạ projectId trong ProjectMemberId
    @JoinColumn(name = "project_id")
    Project project;


    LocalDate joinedDate;

}
