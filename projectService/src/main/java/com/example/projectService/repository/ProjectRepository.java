package com.example.projectService.repository;

import com.example.projectService.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project,String > {

}
