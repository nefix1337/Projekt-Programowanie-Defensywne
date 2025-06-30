package pl.projekt.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.projekt.backend.model.Task;
import pl.projekt.backend.service.TaskService;
import pl.projekt.backend.dto.CreateTaskRequest;
import pl.projekt.backend.dto.TaskCommentResponse;
import pl.projekt.backend.dto.UpdateTaskRequest;
import pl.projekt.backend.dto.TaskWithAssigneeResponse;
import pl.projekt.backend.dto.TaskCreatorDetailsResponse;
import pl.projekt.backend.dto.AddTaskCommentRequest;
import pl.projekt.backend.model.TaskComment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

/**
 * Kontroler REST do obsługi operacji na zadaniach.
 * Udostępnia endpointy do zarządzania zadaniami, pobierania zadań oraz zmiany statusu.
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Zadania", description = "Endpointy zadań")
public class TaskController {

    private final TaskService taskService;

    /**
     * Tworzy nowe zadanie.
     *
     * @param request dane nowego zadania
     * @return odpowiedź HTTP 200 z utworzonym zadaniem
     */
    @Operation(summary = "Tworzenie nowego zadania")
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody CreateTaskRequest request) {
        return ResponseEntity.ok(taskService.createTask(request));
    }

    /**
     * Aktualizuje istniejące zadanie.
     *
     * @param id identyfikator zadania
     * @param request dane do aktualizacji zadania
     * @return odpowiedź HTTP 200 z zaktualizowanym zadaniem
     */
    @Operation(summary = "Aktualizacja zadania")
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody UpdateTaskRequest request) {
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    /**
     * Usuwa zadanie.
     *
     * @param id identyfikator zadania
     * @return odpowiedź HTTP 204 No Content
     */
    @Operation(summary = "Usuwanie zadania")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Pobiera zadania przypisane do projektu.
     *
     * @param projectId identyfikator projektu
     * @return odpowiedź HTTP 200 z listą zadań projektu
     */
    @Operation(summary = "Pobieranie zadań dla projektu")
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Task>> getTasksByProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(taskService.getTasksByProject(projectId));
    }

    /**
     * Pobiera zadania przypisane do aktualnie zalogowanego użytkownika.
     *
     * @return odpowiedź HTTP 200 z listą zadań użytkownika
     */
    @Operation(summary = "Pobieranie zadań zalogowanego użytkownika")
    @GetMapping("/my")
    public ResponseEntity<List<Task>> getMyTasks() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return ResponseEntity.ok(taskService.getTasksForUser(username));
    }

    /**
     * Pobiera wszystkie zadania projektu wraz z przypisanymi użytkownikami (dla MANAGERA).
     *
     * @param projectId identyfikator projektu
     * @return odpowiedź HTTP 200 z listą zadań i przypisanych użytkowników
     */
    @Operation(summary = "Pobieranie wszystkich zadań projektu (dla MANAGERA)")
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/project/{projectId}/all")
    public ResponseEntity<List<TaskWithAssigneeResponse>> getAllTasksForProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(taskService.getAllTasksForProjectWithAssignee(projectId));
    }

    /**
     * Pobiera pojedyncze zadanie wraz z przypisanym użytkownikiem.
     *
     * @param id identyfikator zadania
     * @return odpowiedź HTTP 200 z zadaniem i przypisanym użytkownikiem
     */
    @Operation(summary = "Pobieranie pojedynczego zadania")
    @GetMapping("/{id}")
    public ResponseEntity<TaskWithAssigneeResponse> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskWithAssigneeById(id));
    }

    /**
     * Pobiera szczegóły zadania wraz z informacją o twórcy.
     *
     * @param id identyfikator zadania
     * @return odpowiedź HTTP 200 ze szczegółami zadania i twórcą
     */
    @Operation(summary = "Pobieranie szczegółów zadania z informacją o twórcy")
    @GetMapping("/{id}/details")
    public ResponseEntity<TaskCreatorDetailsResponse> getTaskDetailsById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskCreatorDetailsById(id));
    }

    /**
     * Przekazuje zadanie do sprawdzenia (zmienia status na TO_REVIEW).
     *
     * @param id identyfikator zadania
     * @return odpowiedź HTTP 200 z zadaniem po zmianie statusu
     */
    @Operation(summary = "Przekazanie zadania do sprawdzenia (zmiana statusu na TO_REVIEW)")
    @PatchMapping("/{id}/to-review")
    public ResponseEntity<TaskWithAssigneeResponse> setTaskToReview(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.setTaskStatusToReview(id));
    }

    /**
     * Dodaje komentarz do zadania.
     *
     * @param taskId id zadania
     * @param request treść komentarza
     * @return dodany komentarz
     */
    @Operation(summary = "Dodawanie komentarza do zadania")
    @PostMapping("/{taskId}/comments")
    public ResponseEntity<TaskComment> addCommentToTask(
            @PathVariable Long taskId,
            @RequestBody AddTaskCommentRequest request) {
        return ResponseEntity.ok(taskService.addCommentToTask(taskId, request));
    }

    /**
     * Pobiera komentarze do zadania.
     *
     * @param taskId id zadania
     * @return lista komentarzy z informacją o autorze
     */
    @Operation(summary = "Pobieranie komentarzy do zadania")
    @GetMapping("/{taskId}/comments")
    public ResponseEntity<List<TaskCommentResponse>> getCommentsForTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getCommentsForTask(taskId));
    }
}