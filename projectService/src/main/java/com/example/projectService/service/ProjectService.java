package com.example.projectService.service;

import com.example.projectService.dto.response.ProjectFullResponse;
import com.example.projectService.dto.response.ProjectMemberInfoResponse;
import com.example.projectService.dto.response.ProjectResponse;
import com.example.projectService.dto.response.TaskResponse;
import com.example.projectService.dto.resquest.ProjectCreationRequest;
import com.example.projectService.dto.resquest.ProjectUpdateRequest;
import com.example.projectService.dto.resquest.TaskCreationRequest;
import com.example.projectService.dto.resquest.TaskUpdateRequest;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    @PreAuthorize("hasAnyRole('MANAGER','LEADER')")
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


    @PreAuthorize("hasAnyRole('MANAGER','LEADER')")
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

    @PreAuthorize("hasAnyRole('MANAGER','LEADER')")
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

    @PreAuthorize("hasAnyRole('MANAGER','LEADER')")
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

    private void validateProjectAndMember(String projectId, String userId) {
        if (!projectRepository.existsById(projectId)) {
            throw new AppException(ErrorCode.PROJECT_NOT_FOUND);
        }

        if (!projectMemberRepository.existsById(new ProjectMemberId(projectId, userId))) {
            throw new AppException(ErrorCode.USER_NOT_IN_PROJECT);
        }
    }
}
