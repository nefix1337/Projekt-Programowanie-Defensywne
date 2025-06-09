package pl.projekt.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import pl.projekt.backend.dto.CreateProjectRequest;
import pl.projekt.backend.model.Project;
import pl.projekt.backend.model.ProjectStatus;
import pl.projekt.backend.model.User;
import pl.projekt.backend.repository.ProjectRepository;
import pl.projekt.backend.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

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
}