package pl.projekt.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.projekt.backend.model.Task;
import pl.projekt.backend.service.TaskService;
import pl.projekt.backend.dto.CreateTaskRequest;
import pl.projekt.backend.dto.UpdateTaskRequest;
import pl.projekt.backend.dto.TaskWithAssigneeResponse;
import pl.projekt.backend.dto.TaskCreatorDetailsResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Zadania", description = "Endpointy zadań")
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Tworzenie nowego zadania")
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody CreateTaskRequest request) {
        return ResponseEntity.ok(taskService.createTask(request));
    }

    @Operation(summary = "Aktualizacja zadania")
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody UpdateTaskRequest request) {
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    @Operation(summary = "Usuwanie zadania")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Pobieranie zadań dla projektu")
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Task>> getTasksByProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(taskService.getTasksByProject(projectId));
    }

    @Operation(summary = "Pobieranie zadań zalogowanego użytkownika")
    @GetMapping("/my")
    public ResponseEntity<List<Task>> getMyTasks() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return ResponseEntity.ok(taskService.getTasksForUser(username));
    }

    @Operation(summary = "Pobieranie wszystkich zadań projektu (dla MANAGERA)")
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/project/{projectId}/all")
    public ResponseEntity<List<TaskWithAssigneeResponse>> getAllTasksForProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(taskService.getAllTasksForProjectWithAssignee(projectId));
    }

    @Operation(summary = "Pobieranie pojedynczego zadania")
    @GetMapping("/{id}")
    public ResponseEntity<TaskWithAssigneeResponse> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskWithAssigneeById(id));
    }

    @Operation(summary = "Pobieranie szczegółów zadania z informacją o twórcy")
    @GetMapping("/{id}/details")
    public ResponseEntity<TaskCreatorDetailsResponse> getTaskDetailsById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskCreatorDetailsById(id));
    }
}