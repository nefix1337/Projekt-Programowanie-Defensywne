package com.bsr.messaging;

import com.bsr.model.Task;
import com.bsr.service.TaskCreationService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskCreationListenerTest {
    @Mock
    private TaskCreationService taskCreationService;

    @Test
    void handle_ShouldReturnSuccess_WhenTaskIsCreated() {
        CreateTaskCommand command = new CreateTaskCommand();
        Task task = new Task();
        task.setId(42L);
        TaskCreationListener listener = new TaskCreationListener(taskCreationService);

        when(taskCreationService.createTask(command)).thenReturn(task);

        CreateTaskResult result = listener.handle(command);

        assertTrue(result.isSuccess());
        assertEquals(42L, result.getTaskId());
        assertNull(result.getErrorMessage());
    }

    @Test
    void handle_ShouldReturnFailure_WhenReferencedEntityIsMissing() {
        CreateTaskCommand command = new CreateTaskCommand();
        TaskCreationListener listener = new TaskCreationListener(taskCreationService);

        when(taskCreationService.createTask(command))
                .thenThrow(new EntityNotFoundException("Project not found"));

        CreateTaskResult result = listener.handle(command);

        assertFalse(result.isSuccess());
        assertNull(result.getTaskId());
        assertEquals("Project not found", result.getErrorMessage());
    }
}
