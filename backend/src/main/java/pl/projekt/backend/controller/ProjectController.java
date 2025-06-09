package pl.projekt.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import pl.projekt.backend.dto.CreateProjectRequest;
import pl.projekt.backend.model.Project;
import pl.projekt.backend.service.ProjectService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Projekt", description = "Endpointy projektów")
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "Pobieranie wszystkich projektów użytkownika" )
    @GetMapping
    public ResponseEntity<List<Project>> getAllProjectsForCurrentUser() {
        return ResponseEntity.ok(projectService.getAllProjectsForCurrentUser());
    }

    @Operation(summary = "Pobieranie projektu po ID")
    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable UUID id) {
        return projectService.getProjectById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Tworzenie nowego projektu")
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Project> createProject(@RequestBody CreateProjectRequest request) {
        return ResponseEntity.ok(projectService.createProject(request));
    }

    @Operation(summary = "Aktualizacja projektu")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Project> updateProject(@PathVariable UUID id, @RequestBody Project project) {
        return ResponseEntity.ok(projectService.updateProject(id, project));
    }

    @Operation(summary = "Usuwanie projektu")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}