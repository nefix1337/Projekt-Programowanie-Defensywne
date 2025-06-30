package pl.projekt.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.projekt.backend.dto.*;
import pl.projekt.backend.model.*;
import pl.projekt.backend.repository.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testy jednostkowe serwisu TaskService.
 * Sprawdzają operacje na zadaniach: tworzenie, aktualizacja, usuwanie, pobieranie i zmiana statusu.
 */
@DisplayName("Testy serwisu TaskService")
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private TaskService taskService;

    private User user;
    private User assignedTo;
    private Project project;
    private Task task;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFirstName("Jan");
        user.setLastName("Kowalski");
        user.setEmail("jan.kowalski@example.com");

        assignedTo = new User();
        assignedTo.setId(2L);
        assignedTo.setFirstName("Anna");
        assignedTo.setLastName("Nowak");
        assignedTo.setEmail("anna.nowak@example.com");

        project = new Project();
        project.setId(UUID.randomUUID());
        project.setName("Test Project");

        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Opis zadania");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.HIGH);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setDueDate(LocalDateTime.now().plusDays(1));
        task.setProject(project);
        task.setCreatedBy(user);
        task.setAssignedTo(assignedTo);
    }

    /**
     * Powinien utworzyć nowe zadanie.
     */
    @Test
    @DisplayName("Tworzenie nowego zadania")
    void createTask_ShouldCreateTask() {
        CreateTaskRequest req = new CreateTaskRequest();
        req.setProjectId(project.getId());
        req.setTitle("Test Task");
        req.setDescription("Opis zadania");
        req.setStatus(TaskStatus.TODO);
        req.setPriority(TaskPriority.HIGH);
        req.setDueDate(LocalDateTime.now().plusDays(1));
        req.setAssignedToId(assignedTo.getId());

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.findById(assignedTo.getId())).thenReturn(Optional.of(assignedTo));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

       
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(user.getEmail());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Task result = taskService.createTask(req);

        assertNotNull(result);
        assertEquals("Test Task", result.getTitle());
        verify(taskRepository).save(any(Task.class));
    }

    /**
     * Powinien zaktualizować istniejące zadanie.
     */
    @Test
    @DisplayName("Aktualizacja zadania")
    void updateTask_ShouldUpdateTask() {
        UpdateTaskRequest req = new UpdateTaskRequest();
        req.setTitle("Nowy tytuł");
        req.setDescription("Nowy opis");
        req.setStatus(TaskStatus.IN_PROGRESS);
        req.setPriority(TaskPriority.LOW);
        req.setDueDate(LocalDateTime.now().plusDays(2));
        req.setAssignedToId(assignedTo.getId());

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(userRepository.findById(assignedTo.getId())).thenReturn(Optional.of(assignedTo));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        Task result = taskService.updateTask(task.getId(), req);

        assertNotNull(result);
        assertEquals("Nowy tytuł", result.getTitle());
        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
        assertEquals(TaskPriority.LOW, result.getPriority());
        verify(taskRepository).save(any(Task.class));
    }

    /**
     * Powinien usunąć zadanie.
     */
    @Test
    @DisplayName("Usuwanie zadania")
    void deleteTask_ShouldDeleteTask() {
        taskService.deleteTask(task.getId());
        verify(taskRepository).deleteById(task.getId());
    }

    /**
     * Powinien zwrócić zadania przypisane do użytkownika.
     */
    @Test
    @DisplayName("Pobieranie zadań przypisanych do użytkownika")
    void getTasksForUser_ShouldReturnTasks() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(taskRepository.findByAssignedTo(user)).thenReturn(List.of(task));

        List<Task> result = taskService.getTasksForUser(user.getEmail());

        assertEquals(1, result.size());
        assertEquals(task, result.get(0));
    }

    /**
     * Powinien zwrócić wszystkie zadania projektu.
     */
    @Test
    @DisplayName("Pobieranie wszystkich zadań projektu")
    void getAllTasksForProject_ShouldReturnTasks() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(taskRepository.findByProject(project)).thenReturn(List.of(task));

        List<Task> result = taskService.getAllTasksForProject(project.getId());

        assertEquals(1, result.size());
        assertEquals(task, result.get(0));
    }

    /**
     * Powinien zwrócić zadania projektu przypisane do zalogowanego użytkownika.
     */
    @Test
    @DisplayName("Pobieranie zadań projektu przypisanych do zalogowanego użytkownika")
    void getTasksByProject_ShouldReturnTasks() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(taskRepository.findByProjectAndAssignedTo(project, user)).thenReturn(List.of(task));

        
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(user.getEmail());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        List<Task> result = taskService.getTasksByProject(project.getId());

        assertEquals(1, result.size());
        assertEquals(task, result.get(0));
    }

    /**
     * Powinien zwrócić szczegóły zadania z przypisanym użytkownikiem.
     */
    @Test
    @DisplayName("Pobieranie szczegółów zadania z przypisanym użytkownikiem")
    void getTaskWithAssigneeById_ShouldReturnDetails() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        TaskWithAssigneeResponse response = taskService.getTaskWithAssigneeById(task.getId());

        assertNotNull(response);
        assertEquals(task.getId(), response.getId());
        assertEquals(assignedTo.getFirstName(), response.getAssignedFirstName());
    }

    /**
     * Powinien zwrócić szczegóły zadania z twórcą.
     */
    @Test
    @DisplayName("Pobieranie szczegółów zadania z twórcą")
    void getTaskCreatorDetailsById_ShouldReturnCreatorDetails() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        TaskCreatorDetailsResponse response = taskService.getTaskCreatorDetailsById(task.getId());

        assertNotNull(response);
        assertEquals(user.getFirstName(), response.getCreatorFirstName());
        assertEquals(user.getEmail(), response.getCreatorEmail());
    }

    /**
     * Powinien zmienić status zadania na TO_REVIEW.
     */
    @Test
    @DisplayName("Zmiana statusu zadania na TO_REVIEW")
    void setTaskStatusToReview_ShouldUpdateStatus() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskWithAssigneeResponse response = taskService.setTaskStatusToReview(task.getId());

        assertNotNull(response);
        assertEquals("TO_REVIEW", response.getStatus());
        verify(taskRepository).save(any(Task.class));
    }
}
