package com.bsr.service;

import com.bsr.messaging.AddTaskCommentCommand;
import com.bsr.messaging.CreateTaskCommand;
import com.bsr.messaging.SetTaskStatusCommand;
import com.bsr.messaging.UpdateTaskCommand;
import com.bsr.model.Project;
import com.bsr.model.Task;
import com.bsr.model.TaskComment;
import com.bsr.model.TaskPriority;
import com.bsr.model.TaskStatus;
import com.bsr.model.User;
import com.bsr.repository.ProjectRepository;
import com.bsr.repository.TaskCommentRepository;
import com.bsr.repository.TaskRepository;
import com.bsr.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("Testy serwisu TaskCreationService")
@ExtendWith(MockitoExtension.class)
class TaskCreationServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskCommentRepository taskCommentRepository;

    @Mock
    private DistributedEventService distributedEventService;

    @Mock
    private FaultInjectionService faultInjectionService;

    @InjectMocks
    private TaskCreationService taskCreationService;

    @Test
    @DisplayName("Utworzenie zadania zapisuje je i rejestruje zdarzenie")
    void createTask_PersistsTaskAndRecordsEvent() {
        UUID projectId = UUID.randomUUID();
        CreateTaskCommand command = new CreateTaskCommand();
        command.setProjectId(projectId);
        command.setTitle("New task");
        command.setDescription("desc");
        command.setStatus(TaskStatus.TODO);
        command.setPriority(TaskPriority.MEDIUM);
        command.setAssignedToId(2L);
        command.setCreatedByEmail("manager@example.com");

        Project project = new Project();
        project.setId(projectId);
        User createdBy = new User();
        createdBy.setId(1L);
        createdBy.setEmail("manager@example.com");
        User assignedTo = new User();
        assignedTo.setId(2L);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findByEmail("manager@example.com")).thenReturn(Optional.of(createdBy));
        when(userRepository.findById(2L)).thenReturn(Optional.of(assignedTo));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(10L);
            return task;
        });

        Task result = taskCreationService.createTask(command);

        assertEquals(10L, result.getId());
        assertEquals("New task", result.getTitle());
        assertEquals(assignedTo, result.getAssignedTo());
        verify(faultInjectionService).applyFaults("CREATE_TASK");
        verify(distributedEventService).record("TASK_CREATED", "taskId=10");
    }

    @Test
    @DisplayName("Utworzenie zadania zgłasza wyjątek, gdy projekt nie istnieje")
    void createTask_ThrowsWhenProjectMissing() {
        CreateTaskCommand command = new CreateTaskCommand();
        command.setProjectId(UUID.randomUUID());
        when(projectRepository.findById(command.getProjectId())).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> taskCreationService.createTask(command));

        assertEquals("Project not found", exception.getMessage());
        verify(taskRepository, never()).save(any());
        verify(distributedEventService, never()).record(anyString(), anyString());
    }

    @Test
    @DisplayName("Utworzenie zadania propaguje błąd wstrzyknięty przez mechanizm awarii")
    void createTask_PropagatesFaultInjectionFailure() {
        doThrow(new IllegalStateException("Simulated message corruption while processing: CREATE_TASK"))
                .when(faultInjectionService).applyFaults("CREATE_TASK");

        CreateTaskCommand command = new CreateTaskCommand();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> taskCreationService.createTask(command));

        assertEquals("Simulated message corruption while processing: CREATE_TASK", exception.getMessage());
        verifyNoInteractions(projectRepository, taskRepository, distributedEventService);
    }

    @Test
    @DisplayName("Aktualizacja zadania zmienia podane pola i rejestruje zdarzenie")
    void updateTask_UpdatesProvidedFieldsAndRecordsEvent() {
        Task existing = new Task();
        existing.setId(5L);
        existing.setTitle("Old");
        existing.setStatus(TaskStatus.TODO);
        existing.setPriority(TaskPriority.LOW);

        User newAssignee = new User();
        newAssignee.setId(3L);

        UpdateTaskCommand command = new UpdateTaskCommand();
        command.setTaskId(5L);
        command.setTitle("New title");
        command.setStatus(TaskStatus.IN_PROGRESS);
        command.setAssignedToId(3L);

        when(taskRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(userRepository.findById(3L)).thenReturn(Optional.of(newAssignee));
        when(taskRepository.save(existing)).thenReturn(existing);

        Task result = taskCreationService.updateTask(command);

        assertEquals("New title", result.getTitle());
        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
        assertEquals(newAssignee, result.getAssignedTo());
        assertEquals(TaskPriority.LOW, result.getPriority());
        verify(faultInjectionService).applyFaults("UPDATE_TASK");
        verify(distributedEventService).record("TASK_UPDATED", "taskId=5");
    }

    @Test
    @DisplayName("Aktualizacja zadania zgłasza wyjątek, gdy zadanie nie istnieje")
    void updateTask_ThrowsWhenTaskMissing() {
        UpdateTaskCommand command = new UpdateTaskCommand();
        command.setTaskId(99L);
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> taskCreationService.updateTask(command));
        verify(distributedEventService, never()).record(anyString(), anyString());
    }

    @Test
    @DisplayName("Usunięcie zadania usuwa je wraz z komentarzami i rejestruje zdarzenie")
    void deleteTask_RemovesTaskAndCommentsAndRecordsEvent() {
        Task task = new Task();
        task.setId(7L);
        when(taskRepository.findById(7L)).thenReturn(Optional.of(task));

        taskCreationService.deleteTask(7L);

        verify(faultInjectionService).applyFaults("DELETE_TASK");
        verify(taskCommentRepository).deleteByTask(task);
        verify(taskRepository).delete(task);
        verify(distributedEventService).record("TASK_DELETED", "taskId=7");
    }

    @Test
    @DisplayName("Zmiana statusu zadania aktualizuje status i rejestruje zdarzenie")
    void setTaskStatus_UpdatesStatusAndRecordsEvent() {
        Task task = new Task();
        task.setId(8L);
        task.setStatus(TaskStatus.TODO);

        SetTaskStatusCommand command = new SetTaskStatusCommand(8L, TaskStatus.DONE);

        when(taskRepository.findById(8L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskCreationService.setTaskStatus(command);

        assertEquals(TaskStatus.DONE, result.getStatus());
        verify(faultInjectionService).applyFaults("SET_TASK_STATUS");
        verify(distributedEventService).record("TASK_STATUS_CHANGED", "taskId=8,status=DONE");
    }

    @Test
    @DisplayName("Dodanie komentarza zapisuje go i rejestruje zdarzenie")
    void addComment_SavesCommentAndRecordsEvent() {
        Task task = new Task();
        task.setId(9L);
        User author = new User();
        author.setId(4L);
        author.setEmail("tester@example.com");

        AddTaskCommentCommand command = new AddTaskCommentCommand(9L, "Looks good", "tester@example.com");

        when(taskRepository.findById(9L)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail("tester@example.com")).thenReturn(Optional.of(author));
        when(taskCommentRepository.save(any(TaskComment.class))).thenAnswer(invocation -> {
            TaskComment comment = invocation.getArgument(0);
            comment.setId(100L);
            return comment;
        });

        TaskComment result = taskCreationService.addComment(command);

        assertEquals("Looks good", result.getComment());
        assertEquals(task, result.getTask());
        assertEquals(author, result.getUser());
        verify(faultInjectionService).applyFaults("ADD_COMMENT");
        verify(distributedEventService).record("TASK_COMMENT_ADDED", "taskId=9,commentId=100");
    }
}
