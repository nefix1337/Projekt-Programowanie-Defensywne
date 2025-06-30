package pl.projekt.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import pl.projekt.backend.dto.CreateProjectRequest;
import pl.projekt.backend.dto.ProjectMemberResponse;
import pl.projekt.backend.dto.AddProjectMemberRequest;
import pl.projekt.backend.model.Project;
import pl.projekt.backend.service.ProjectService;

import java.util.List;
import java.util.UUID;

/**
 * Kontroler REST do obsługi operacji na projektach.
 * Udostępnia endpointy do zarządzania projektami oraz członkami projektów.
 */
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Projekt", description = "Endpointy projektów")
public class ProjectController {

    private final ProjectService projectService;

    /**
     * Pobiera wszystkie projekty utworzone przez aktualnie zalogowanego użytkownika.
     *
     * @return odpowiedź HTTP 200 z listą projektów
     */
    @Operation(summary = "Pobieranie wszystkich projektów użytkownika")
    @GetMapping
    public ResponseEntity<List<Project>> getAllProjectsForCurrentUser() {
        return ResponseEntity.ok(projectService.getAllProjectsForCurrentUser());
    }

    /**
     * Pobiera projekt po jego identyfikatorze.
     *
     * @param id identyfikator projektu
     * @return odpowiedź HTTP 200 z projektem lub 404 jeśli nie znaleziono
     */
    @Operation(summary = "Pobieranie projektu po ID")
    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable UUID id) {
        return projectService.getProjectById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Tworzy nowy projekt.
     * Endpoint dostępny tylko dla roli MANAGER.
     *
     * @param request dane nowego projektu
     * @return odpowiedź HTTP 200 z utworzonym projektem
     */
    @Operation(summary = "Tworzenie nowego projektu")
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Project> createProject(@RequestBody CreateProjectRequest request) {
        return ResponseEntity.ok(projectService.createProject(request));
    }

    /**
     * Aktualizuje istniejący projekt.
     * Endpoint dostępny tylko dla roli MANAGER.
     *
     * @param id identyfikator projektu
     * @param project zaktualizowane dane projektu
     * @return odpowiedź HTTP 200 z zaktualizowanym projektem
     */
    @Operation(summary = "Aktualizacja projektu")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Project> updateProject(@PathVariable UUID id, @RequestBody Project project) {
        return ResponseEntity.ok(projectService.updateProject(id, project));
    }

    /**
     * Usuwa projekt.
     * Endpoint dostępny tylko dla roli MANAGER.
     *
     * @param id identyfikator projektu
     * @return odpowiedź HTTP 204 No Content
     */
    @Operation(summary = "Usuwanie projektu")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Dodaje użytkownika do projektu.
     * Endpoint dostępny tylko dla roli MANAGER.
     *
     * @param projectId identyfikator projektu
     * @param request dane użytkownika do dodania
     * @return odpowiedź HTTP 200 z informacją o dodanym członku projektu
     */
    @Operation(summary = "Dodawanie użytkownika do projektu")
    @PostMapping("/{projectId}/members")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ProjectMemberResponse> addProjectMember(
            @PathVariable UUID projectId,
            @RequestBody AddProjectMemberRequest request) {
        return ResponseEntity.ok(projectService.addProjectMember(projectId, request));
    }

    /**
     * Pobiera listę członków projektu.
     *
     * @param projectId identyfikator projektu
     * @return odpowiedź HTTP 200 z listą członków projektu
     */
    @Operation(summary = "Pobieranie członków projektu")
    @GetMapping("/{projectId}/members")
    public ResponseEntity<List<ProjectMemberResponse>> getProjectMembers(@PathVariable UUID projectId) {
        return ResponseEntity.ok(projectService.getProjectMembers(projectId));
    }

    /**
     * Usuwa użytkownika z projektu.
     * Endpoint dostępny tylko dla roli MANAGER.
     *
     * @param projectId identyfikator projektu
     * @param userId identyfikator użytkownika
     * @return odpowiedź HTTP 204 No Content
     */
    @Operation(summary = "Usuwanie użytkownika z projektu")
    @DeleteMapping("/{projectId}/members/{userId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> removeProjectMember(
            @PathVariable UUID projectId,
            @PathVariable Long userId) {
        projectService.removeProjectMember(projectId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Pobiera projekty, w których aktualnie zalogowany użytkownik jest członkiem.
     *
     * @return odpowiedź HTTP 200 z listą projektów
     */
    @Operation(summary = "Pobieranie projektów, w których jestem członkiem")
    @GetMapping("/my-member-projects")
    public ResponseEntity<List<Project>> getProjectsWhereCurrentUserIsMember() {
        return ResponseEntity.ok(projectService.getProjectsWhereCurrentUserIsMember());
    }
}