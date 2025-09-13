package com.example.projectService.repository;

import com.example.projectService.dto.response.TaskDTOResponse;
import com.example.projectService.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task,String > {
    @Modifying
    @Query("DELETE FROM Task t WHERE t.assignedTo = :assigneeId AND t.project.id = :projectId")
    void deleteByAssigneeIdAndProjectId(@Param("assigneeId") String assigneeId, @Param("projectId") String projectId);

//    @Query("SELECT t FROM Task t WHERE t.project.id IN :projectIds AND t.assignedTo = :assignedTo")
//    List<Task> findByProjectIdsAndAssigneeTo(@Param("projectIds") List<String> projectIds,
//                                             @Param("assignedTo") String assignedTo);

//@Query("""
//        SELECT t.id AS id, t.title AS title, t.status AS status\
//        FROM Task t WHERE t.project.id IN :projectIds AND t.assignedTo = :assignedTo""")
//List<TaskDTOResponse> findByProjectIdsAndAssigneeTo(@Param("projectIds") List<String> projectIds,
//                                                    @Param("assignedTo") String assignedTo);

    @Query("""
       SELECT t.id AS id, t.title AS title, t.status AS status, t.description AS description
       FROM Task t
       WHERE t.project.id IN :projectIds AND t.assignedTo = :assignedTo
       """)
    List<TaskDTOResponse> findByProjectIdsAndAssigneeTo(@Param("projectIds") List<String> projectIds,
                                                        @Param("assignedTo") String assignedTo);




}
