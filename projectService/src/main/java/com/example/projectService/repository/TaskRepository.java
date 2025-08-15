package com.example.projectService.repository;

import com.example.projectService.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface TaskRepository extends JpaRepository<Task,String > {
    @Modifying
    @Query("DELETE FROM Task t WHERE t.assignedTo = :assigneeId AND t.project.id = :projectId")
    void deleteByAssigneeIdAndProjectId(@Param("assigneeId") String assigneeId, @Param("projectId") String projectId);
}
