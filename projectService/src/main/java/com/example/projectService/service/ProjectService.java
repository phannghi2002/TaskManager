package com.example.projectService.service;

import com.example.projectService.dto.response.*;
import com.example.projectService.dto.resquest.*;
import com.example.projectService.entity.Project;
import com.example.projectService.entity.ProjectMember;
import com.example.projectService.entity.ProjectMemberId;
import com.example.projectService.entity.Task;
import com.example.projectService.enums.ProjectStatus;
import com.example.projectService.enums.TaskStatus;
import com.example.projectService.exception.AppException;
import com.example.projectService.exception.ErrorCode;
import com.example.projectService.mapper.ProjectMapper;
import com.example.projectService.repository.ProjectMemberRepository;
import com.example.projectService.repository.ProjectRepository;
import com.example.projectService.repository.TaskRepository;
import com.example.projectService.repository.httpclient.ProfileClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectService {
    ProjectRepository projectRepository;

    ProjectMemberRepository projectMemberRepository;

    ProfileClient profileClient;

    TaskRepository taskRepository;

    KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    public String getUserIdFromToken() {
        var context = SecurityContextHolder.getContext();

        var authentication = context.getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();

            String subject = jwt.getSubject();
            Map<String, Object> claims = jwt.getClaims(); // All claims

            return (String) claims.get("userId");
        } else {
            throw new IllegalStateException("No JWT token found in SecurityContext");
        }
    }


    public ProjectResponse createProject(ProjectCreationRequest request) {
        String userId = getUserIdFromToken();

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .createBy(userId)
                .status(ProjectStatus.PLANNING)
                .build();

        Project saved = projectRepository.save(project);

        return ProjectResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .description(saved.getDescription())
                .startDate(saved.getStartDate())
                .endDate(saved.getEndDate())
                .createBy(saved.getCreateBy())
                .build();
    }



    public ProjectResponse updateProject(String projectId, ProjectUpdateRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));


        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }

        Project updatedProject = project.toBuilder()
                .name(request.getName() != null ? request.getName() : project.getName())
                .description(request.getDescription() != null ? request.getDescription() : project.getDescription())
                .startDate(request.getStartDate() != null ? request.getStartDate() : project.getStartDate())
                .endDate(request.getEndDate() != null ? request.getEndDate() : project.getEndDate())
                .createBy(project.getCreateBy())
//                .status(request.getStatus() != null ? request.getStatus() : project.getStatus())
                .build();

        projectRepository.save(updatedProject);

        return ProjectResponse.builder()
                .id(updatedProject.getId())
                .name(updatedProject.getName())
                .description(updatedProject.getDescription())
                .startDate(updatedProject.getStartDate())
                .endDate(updatedProject.getEndDate())
                .createBy(updatedProject.getCreateBy())
                .status(updatedProject.getStatus())
                .build();
    }

    //dung cai nay thi nó sẽ fetch được thông tin từ member vì member
    // dung lazy
    @Transactional(readOnly = true)
    public ProjectFullResponse getProject(String projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(()
                -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        return ProjectMapper.toFullResponse(project);

    }

    @Transactional(readOnly = true)
    public List<ProjectFullResponse> getAllProject() {
        List<Project> projects = projectRepository.findAll();

        if (projects.isEmpty()) throw new AppException(ErrorCode.PROJECT_NOT_FOUND);

        return projects.stream()
                .map(ProjectMapper::toFullResponse)
                .collect(Collectors.toList())
                ;

    }


    public void addMember(String projectId, String userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        if (!profileClient.checkProfile(userId)) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        ProjectMemberId pmId = new ProjectMemberId(projectId, userId);

        if (projectMemberRepository.existsById(pmId)) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        ProjectMember member = ProjectMember.builder()
                .id(pmId)
                .project(project)
                .joinedDate(LocalDate.now())
                .build();

        projectMemberRepository.save(member);
    }

    @PreAuthorize("hasAnyRole('MANAGER','LEADER')")
    public void addMembers(String projectId, List<String> userIds) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        for (String userId : userIds) {
            if (!profileClient.checkProfile(userId)) {
                throw new AppException(ErrorCode.USER_NOT_EXISTED);
            }

            ProjectMemberId pmId = new ProjectMemberId(projectId, userId);

            if (projectMemberRepository.existsById(pmId)) {
                throw new AppException(ErrorCode.USER_EXISTED);
            }

            ProjectMember member = ProjectMember.builder()
                    .id(pmId)
                    .project(project)
                    .joinedDate(LocalDate.now())
                    .build();

            projectMemberRepository.save(member);
        }
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER','LEADER')")
    public void deleteMember(String projectId, String userId) {
        validateProjectAndMember(projectId, userId);

        ProjectMemberId projectMemberId = ProjectMemberId.builder()
                .projectId(projectId)
                .userId(userId)
                .build();

        projectMemberRepository.deleteById(projectMemberId);

        taskRepository.deleteByAssigneeIdAndProjectId(userId, projectId);

    }


    public void deleteProject(String projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new AppException(ErrorCode.PROJECT_NOT_FOUND);
        }
        projectRepository.deleteById(projectId);
    }

    @Transactional
//    @PreAuthorize("hasAnyRole('MANAGER','LEADER')")
    public ProjectMemberInfoResponse getMemberFromProject(String projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        if (project.getMembers().isEmpty()) new AppException(ErrorCode.PROJECT_NOW_NOT_HAVE_MEMBER);

        List<String> memberIds = project.getMembers()
                .stream()
                .map(m -> m.getId().getUserId())
                .toList();

        return ProjectMemberInfoResponse.builder()
                .projectName(project.getName())
                .memberIds(memberIds)
                .build();
    }

    public List<String> getListIdMemberFromProject(String projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        if (project.getMembers().isEmpty()) new AppException(ErrorCode.PROJECT_NOW_NOT_HAVE_MEMBER);

        List<String> memberIds = project.getMembers()
                .stream()
                .map(m -> m.getId().getUserId())
                .toList();

        return memberIds;

    }

    @PreAuthorize("hasAnyRole('MANAGER','LEADER')")
    @Transactional
    public void addTask(String projectId, TaskCreationRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        List<String> memberIds = project.getMembers()
                .stream()
                .map(m -> m.getId().getUserId())
                .toList();

        if (!memberIds.contains(request.getAssigneeId())) {
            throw new AppException(ErrorCode.USER_NOT_IN_PROJECT);
        }

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(TaskStatus.TO_DO)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .createBy(getUserIdFromToken())
                .assignedTo(request.getAssigneeId())
                .project(project)
                .build();

        taskRepository.save(task);
    }

    @PreAuthorize("hasAnyRole('MANAGER','LEADER')")
    public void deleteTask(String projectId, String taskId) {
        if (!projectRepository.existsById(projectId)) {
            throw new AppException(ErrorCode.PROJECT_NOT_FOUND);
        }

        if (!taskRepository.existsById(taskId)) {
            throw new AppException(ErrorCode.TASK_NOT_EXISTED);
        }

        taskRepository.deleteById(taskId);

    }

    public TaskResponse updateTask(String projectId, String taskId, TaskUpdateRequest request) {
        if (!projectRepository.existsById(projectId)) {
            throw new AppException(ErrorCode.PROJECT_NOT_FOUND);
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_EXISTED));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isManagerOrLeader = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER") || a.getAuthority().equals("ROLE_LEADER"));

        if (!isManagerOrLeader) {
            if (request.getTitle() != null
                    || request.getDescription() != null
                    || request.getStartDate() != null
                    || request.getEndDate() != null
                    || request.getAssigneeId() != null) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        }

        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }

        if (isManagerOrLeader) {
            if (request.getTitle() != null) task.setTitle(request.getTitle());
            if (request.getDescription() != null) task.setDescription(request.getDescription());
            if (request.getStartDate() != null) task.setStartDate(request.getStartDate());
            if (request.getEndDate() != null) task.setEndDate(request.getEndDate());
            if (request.getAssigneeId() != null) {
                ProjectMemberId projectMemberId = ProjectMemberId.builder()
                        .projectId(projectId)
                        .userId(request.getAssigneeId())
                        .build();

                if (!projectMemberRepository.existsById(projectMemberId)) {
                    throw new AppException(ErrorCode.USER_NOT_IN_PROJECT);
                }
                task.setAssignedTo(request.getAssigneeId());
            }
        }

        taskRepository.save(task);

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .assigneeId(task.getAssignedTo())
                .deadline(task.getEndDate())
                .build();
    }


//    public List<ProjectWithTasksResponse> getProjectsWithUserTasks(String userId) {
//
//        List<ProjectDTOResponse> projects = findProjectsByMember(userId);
//
//        if (projects.isEmpty()) {
//            return List.of(); // Không có project nào
//        }
//        List<String> projectIds = projects.stream()
//                .map(ProjectDTOResponse::getId)
//                .toList();
//
//        List<TaskDTOResponse> userTasks = findByProjectIdsAndAssigneeTo(projectIds, userId);
//
//
//        return projects.stream()
//                .map(p -> ProjectWithTasksResponse.builder()
//                        .id(p.getId())
//                        .name(p.getName())
//                        .description(p.getDescription())
//                        .tasks(userTasks)
//                        .build()
//                )
//                .toList();
//
//    }

    public List<ProjectWithTasksResponse> getProjectsWithUserTasks(String userId) {
        List<Project> projects = projectRepository.findProjectsWithTasksOfUser(userId);

        return projects.stream().map(p ->
                ProjectWithTasksResponse.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .description(p.getDescription())
                        .tasks(
                                p.getTasks().stream()
                                        .map(t -> TaskDTOResponseImpl.builder()
                                                .id(t.getId())
                                                .title(t.getTitle())
                                                .description(t.getDescription())
                                                .endDate(t.getEndDate())
                                                .status(t.getStatus())
                                                .build()
                                        ).toList()
                        )
                        .build()
        ).toList();
    }


    public List<ProjectWithTasksResponse> getProjectsWithMyTasks() {
        String userId = getUserIdFromToken();

        return getProjectsWithUserTasks(userId);

    }

    private void validateProjectAndMember(String projectId, String userId) {
        if (!projectRepository.existsById(projectId)) {
            throw new AppException(ErrorCode.PROJECT_NOT_FOUND);
        }

        if (!projectMemberRepository.existsById(new ProjectMemberId(projectId, userId))) {
            throw new AppException(ErrorCode.USER_NOT_IN_PROJECT);
        }
    }


    public List<ProjectDTOResponse> findProjectsByMember(String userId) {
        return projectRepository.findByMembersContaining(userId);
    }


//    public List<Task> findByProjectIdsAndAssigneeTo(List<String> projectIds, String assignedTo) {
//        if (projectIds == null || projectIds.isEmpty()) {
//            return List.of();
//        }
//        return taskRepository.findByProjectIdsAndAssigneeTo(projectIds, assignedTo);
//    }

    public List<TaskDTOResponse> findByProjectIdsAndAssigneeTo(List<String> projectIds, String assignedTo) {
        if (projectIds == null || projectIds.isEmpty()) {
            return List.of();
        }
        return taskRepository.findByProjectIdsAndAssigneeTo(projectIds, assignedTo);
    }

//    public void sendEventListIdMember(Map<String, Object> event) {
//
//        kafkaTemplate.send("listId-member-events", event);
//    }
//
//    public Object getMemberNotInProject(String projectId){
//        List<String> memberIds = getListIdMemberFromProject(projectId);
//
//        Map<String, Object> event = new HashMap<>();
//
//        event.put("listIds", memberIds);
//
//        sendEventListIdMember(event);
//    }

    public ApiResponse<List<NotMemberResponse>> getNotMemberInProject(NotMemberRequest request){
        return profileClient.getNotMember(request);

    }

}
