package com.example.projectService.repository;

import com.example.projectService.entity.ProjectMember;
import com.example.projectService.entity.ProjectMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {
}
