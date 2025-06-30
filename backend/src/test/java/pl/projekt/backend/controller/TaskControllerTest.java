package pl.projekt.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import pl.projekt.backend.service.TaskService;
import pl.projekt.backend.model.Task;
import pl.projekt.backend.dto.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Testy kontrolera zadań")
@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    private Task testTask;
    private TaskCreatorDetailsResponse testTaskCreatorDetails;
    private TaskWithAssigneeResponse testTaskWithAssignee;

    @BeforeEach
    void setUp() {
        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");

        testTaskCreatorDetails = new TaskCreatorDetailsResponse(
            1L, "Test Task", "Description", "OPEN", "HIGH",
            LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now().plusDays(1),
            "John", "Doe", "john.doe@example.com"
        );

        testTaskWithAssignee = new TaskWithAssigneeResponse(
            1L, "Test Task", "Description", "OPEN", "HIGH",
            LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now().plusDays(1),
            "Jane", "Smith"
        );
    }

    @Test
    @DisplayName("Powinien utworzyć nowe zadanie i zwrócić 200 OK")
    void createTask_ShouldReturnCreatedTask() {
        CreateTaskRequest request = new CreateTaskRequest();
        when(taskService.createTask(request)).thenReturn(testTask);

        ResponseEntity<Task> response = taskController.createTask(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(testTask, response.getBody());
        verify(taskService).createTask(request);
    }

    @Test
    @DisplayName("Powinien zwrócić szczegóły zadania z informacją o twórcy i 200 OK")
    void getTaskDetailsById_ShouldReturnTaskDetails() {
        when(taskService.getTaskCreatorDetailsById(1L)).thenReturn(testTaskCreatorDetails);

        ResponseEntity<TaskCreatorDetailsResponse> response = taskController.getTaskDetailsById(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(testTaskCreatorDetails, response.getBody());
        assertEquals("John", response.getBody().getCreatorFirstName());
        verify(taskService).getTaskCreatorDetailsById(1L);
    }

    @Test
    @DisplayName("Powinien zwrócić listę zadań dla projektu z przypisanymi osobami i 200 OK")
    void getAllTasksForProject_ShouldReturnTasksList() {
        UUID projectId = UUID.randomUUID();
        List<TaskWithAssigneeResponse> tasks = Arrays.asList(testTaskWithAssignee);
        when(taskService.getAllTasksForProjectWithAssignee(projectId)).thenReturn(tasks);

        ResponseEntity<List<TaskWithAssigneeResponse>> response = taskController.getAllTasksForProject(projectId);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals(testTaskWithAssignee, response.getBody().get(0));
        verify(taskService).getAllTasksForProjectWithAssignee(projectId);
    }

    @Test
    @DisplayName("Powinien zwrócić pojedyncze zadanie z informacją o przypisanej osobie i 200 OK")
    void getTaskById_ShouldReturnTaskWithAssignee() {
        when(taskService.getTaskWithAssigneeById(1L)).thenReturn(testTaskWithAssignee);

        ResponseEntity<TaskWithAssigneeResponse> response = taskController.getTaskById(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(testTaskWithAssignee, response.getBody());
        assertEquals("Jane", response.getBody().getAssignedFirstName());
        verify(taskService).getTaskWithAssigneeById(1L);
    }

    @Test
    @DisplayName("Powinien usunąć zadanie i zwrócić 204 No Content")
    void deleteTask_ShouldReturnNoContent() {
        ResponseEntity<Void> response = taskController.deleteTask(1L);

        assertNotNull(response);
        assertEquals(204, response.getStatusCode().value());
        verify(taskService).deleteTask(1L);
    }
}