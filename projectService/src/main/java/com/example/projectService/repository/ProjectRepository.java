package com.example.projectService.repository;

import com.example.projectService.dto.response.ProjectDTOResponse;
import com.example.projectService.dto.response.ProjectWithTasksResponse;
import com.example.projectService.dto.response.TaskDTOResponse;
import com.example.projectService.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project,String > {
    @Query("SELECT p FROM Project p JOIN p.members m WHERE m.id.userId = :userId")
    List<ProjectDTOResponse> findByMembersContaining(@Param("userId") String userId);


    @Query("""
    SELECT DISTINCT p
    FROM Project p
    JOIN p.members m
    LEFT JOIN FETCH p.tasks t
    WHERE m.id.userId = :userId 
      AND (t.assignedTo = :userId OR t.assignedTo IS NULL)
""")
    List<Project> findProjectsWithTasksOfUser(@Param("userId") String userId);

}
