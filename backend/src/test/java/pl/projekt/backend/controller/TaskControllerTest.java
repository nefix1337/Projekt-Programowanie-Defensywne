package pl.projekt.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import pl.projekt.backend.dto.*;
import pl.projekt.backend.model.Task;
import pl.projekt.backend.model.TaskPriority;
import pl.projekt.backend.model.TaskStatus;
import pl.projekt.backend.service.TaskService;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testy jednostkowe kontrolera TaskController.
 * Sprawdzają obsługę endpointów związanych z zadaniami.
 */
@DisplayName("Testy kontrolera TaskController")
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    private Task task;
    private TaskWithAssigneeResponse taskWithAssignee;
    private TaskCreatorDetailsResponse taskCreatorDetails;
    private List<Task> taskList;
    private List<TaskWithAssigneeResponse> taskWithAssigneeList;

    @BeforeEach
    void setUp() {
        task = new Task();
        task.setId(1L);
        task.setTitle("Testowe zadanie");
        task.setDescription("Opis zadania");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.HIGH);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setDueDate(LocalDateTime.now().plusDays(1));

        taskWithAssignee = new TaskWithAssigneeResponse(
                1L, "Testowe zadanie", "Opis zadania", "TODO", "HIGH",
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                "Jan", "Kowalski"
        );

        taskCreatorDetails = new TaskCreatorDetailsResponse(
                1L, "Testowe zadanie", "Opis zadania", "TODO", "HIGH",
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                "Jan", "Kowalski", "jan.kowalski@example.com"
        );

        taskList = List.of(task);
        taskWithAssigneeList = List.of(taskWithAssignee);
    }

    /**
     * Powinien utworzyć nowe zadanie i zwrócić odpowiedź 200 OK.
     */
    @Test
    @DisplayName("Tworzenie nowego zadania")
    void createTask_ShouldReturnCreatedTask() {
        CreateTaskRequest request = new CreateTaskRequest();
        when(taskService.createTask(request)).thenReturn(task);

        ResponseEntity<Task> response = taskController.createTask(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(task, response.getBody());
        verify(taskService).createTask(request);
    }

    /**
     * Powinien zaktualizować zadanie i zwrócić odpowiedź 200 OK.
     */
    @Test
    @DisplayName("Aktualizacja zadania")
    void updateTask_ShouldReturnUpdatedTask() {
        UpdateTaskRequest request = new UpdateTaskRequest();
        when(taskService.updateTask(1L, request)).thenReturn(task);

        ResponseEntity<Task> response = taskController.updateTask(1L, request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(task, response.getBody());
        verify(taskService).updateTask(1L, request);
    }

    /**
     * Powinien usunąć zadanie i zwrócić odpowiedź 204 No Content.
     */
    @Test
    @DisplayName("Usuwanie zadania")
    void deleteTask_ShouldReturnNoContent() {
        ResponseEntity<Void> response = taskController.deleteTask(1L);

        assertNotNull(response);
        assertEquals(204, response.getStatusCodeValue());
        verify(taskService).deleteTask(1L);
    }

    /**
     * Powinien zwrócić listę zadań dla projektu.
     */
    @Test
    @DisplayName("Pobieranie zadań dla projektu")
    void getTasksByProject_ShouldReturnTasks() {
        UUID projectId = UUID.randomUUID();
        when(taskService.getTasksByProject(projectId)).thenReturn(taskList);

        ResponseEntity<List<Task>> response = taskController.getTasksByProject(projectId);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(taskList, response.getBody());
        verify(taskService).getTasksByProject(projectId);
    }

    /**
     * Powinien zwrócić listę zadań zalogowanego użytkownika.
     */
    @Test
    @DisplayName("Pobieranie zadań zalogowanego użytkownika")
    void getMyTasks_ShouldReturnTasksForUser() {
        // Symulacja SecurityContextHolder
        org.springframework.security.core.context.SecurityContextHolder.getContext()
                .setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("jan.kowalski@example.com", null));
        when(taskService.getTasksForUser("jan.kowalski@example.com")).thenReturn(taskList);

        ResponseEntity<List<Task>> response = taskController.getMyTasks();

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(taskList, response.getBody());
        verify(taskService).getTasksForUser("jan.kowalski@example.com");
    }

    /**
     * Powinien zwrócić wszystkie zadania projektu (dla MANAGERA).
     */
    @Test
    @DisplayName("Pobieranie wszystkich zadań projektu (dla MANAGERA)")
    void getAllTasksForProject_ShouldReturnTasksWithAssignee() {
        UUID projectId = UUID.randomUUID();
        when(taskService.getAllTasksForProjectWithAssignee(projectId)).thenReturn(taskWithAssigneeList);

        ResponseEntity<List<TaskWithAssigneeResponse>> response = taskController.getAllTasksForProject(projectId);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(taskWithAssigneeList, response.getBody());
        verify(taskService).getAllTasksForProjectWithAssignee(projectId);
    }

    /**
     * Powinien zwrócić pojedyncze zadanie z przypisanym użytkownikiem.
     */
    @Test
    @DisplayName("Pobieranie pojedynczego zadania")
    void getTaskById_ShouldReturnTaskWithAssignee() {
        when(taskService.getTaskWithAssigneeById(1L)).thenReturn(taskWithAssignee);

        ResponseEntity<TaskWithAssigneeResponse> response = taskController.getTaskById(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(taskWithAssignee, response.getBody());
        verify(taskService).getTaskWithAssigneeById(1L);
    }

    /**
     * Powinien zwrócić szczegóły zadania z informacją o twórcy.
     */
    @Test
    @DisplayName("Pobieranie szczegółów zadania z informacją o twórcy")
    void getTaskDetailsById_ShouldReturnTaskCreatorDetails() {
        when(taskService.getTaskCreatorDetailsById(1L)).thenReturn(taskCreatorDetails);

        ResponseEntity<TaskCreatorDetailsResponse> response = taskController.getTaskDetailsById(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(taskCreatorDetails, response.getBody());
        verify(taskService).getTaskCreatorDetailsById(1L);
    }

    /**
     * Powinien zmienić status zadania na TO_REVIEW i zwrócić odpowiedź 200 OK.
     */
    @Test
    @DisplayName("Zmiana statusu zadania na TO_REVIEW")
    void setTaskToReview_ShouldReturnTaskWithStatusToReview() {
        when(taskService.setTaskStatusToReview(1L)).thenReturn(taskWithAssignee);

        ResponseEntity<TaskWithAssigneeResponse> response = taskController.setTaskToReview(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(taskWithAssignee, response.getBody());
        verify(taskService).setTaskStatusToReview(1L);
    }
}