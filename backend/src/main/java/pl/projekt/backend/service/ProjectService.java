package pl.projekt.backend.service;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import pl.projekt.backend.dto.CreateProjectRequest;
import pl.projekt.backend.dto.ProjectMemberResponse;
import pl.projekt.backend.dto.AddProjectMemberRequest;
import pl.projekt.backend.model.Project;
import pl.projekt.backend.model.ProjectMember;
import pl.projekt.backend.model.ProjectStatus;
import pl.projekt.backend.model.User;
import pl.projekt.backend.repository.ProjectRepository;
import pl.projekt.backend.repository.UserRepository;
import pl.projekt.backend.repository.ProjectMemberRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public List<Project> getAllProjectsForCurrentUser() {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return projectRepository.findByCreatedBy(currentUser);
    }

    public Optional<Project> getProjectById(UUID id) {
        return projectRepository.findById(id);
    }

    public Project createProject(CreateProjectRequest request) {
    String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
    User currentUser = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));

    Project project = new Project();
    project.setName(request.getName());
    project.setDescription(request.getDescription());
    project.setStatus(ProjectStatus.valueOf(request.getStatus().toUpperCase()));
    project.setIcon(request.getIcon());
    project.setCreatedBy(currentUser);

    return projectRepository.save(project);
}

    public Project updateProject(UUID id, Project updatedProject) {
        return projectRepository.findById(id)
                .map(existingProject -> {
                    existingProject.setName(updatedProject.getName());
                    existingProject.setDescription(updatedProject.getDescription());
                    existingProject.setStatus(updatedProject.getStatus());
                    return projectRepository.save(existingProject);
                })
                .orElseThrow(() -> new RuntimeException("Project not found"));
    }

    public void deleteProject(UUID id) {
        projectRepository.deleteById(id);
    }

    public ProjectMemberResponse addProjectMember(UUID projectId, AddProjectMemberRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        User user = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Zamiast iterować po project.getMembers(), sprawdź w repozytorium:
        if (projectMemberRepository.findByProjectAndUser(project, user).isPresent()) {
            throw new RuntimeException("User is already a member of this project");
        }

        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(user);
        member.setProjectRole(request.getProjectRole());
        member.setJoinedAt(LocalDateTime.now());

        ProjectMember savedMember = projectMemberRepository.save(member);

        return new ProjectMemberResponse(
            savedMember.getId(),
            savedMember.getUser().getId(),        // userId
            savedMember.getUser().getEmail(),
            savedMember.getUser().getFirstName(),
            savedMember.getUser().getLastName(),
            savedMember.getProjectRole(),
            savedMember.getJoinedAt()
        );
    }

    public List<ProjectMemberResponse> getProjectMembers(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        return projectMemberRepository.findByProject(project)
                .stream()
                .map(member -> new ProjectMemberResponse(
    member.getId(),
    member.getUser().getId(),             // userId
    member.getUser().getEmail(),
    member.getUser().getFirstName(),
    member.getUser().getLastName(),
    member.getProjectRole(),
    member.getJoinedAt()
))
                .toList();
    }

    public void removeProjectMember(UUID projectId, Long userId) {
    Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    ProjectMember member = projectMemberRepository.findByProjectAndUser(project, user)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User is not a member of this project"));

    projectMemberRepository.delete(member);
}

    public List<Project> getProjectsWhereCurrentUserIsMember() {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ProjectMember> memberships = projectMemberRepository.findByUser(currentUser);

        return memberships.stream()
                .map(ProjectMember::getProject)
                .distinct()
                .toList();
    }
}