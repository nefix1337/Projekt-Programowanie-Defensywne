package com.bsr.service;

import com.bsr.messaging.CreateTaskCommand;
import com.bsr.messaging.AddTaskCommentCommand;
import com.bsr.messaging.SetTaskStatusCommand;
import com.bsr.messaging.UpdateTaskCommand;
import com.bsr.model.Project;
import com.bsr.model.Task;
import com.bsr.model.TaskComment;
import com.bsr.model.User;
import com.bsr.repository.ProjectRepository;
import com.bsr.repository.TaskCommentRepository;
import com.bsr.repository.TaskRepository;
import com.bsr.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskCreationService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final DistributedEventService distributedEventService;

    @Transactional
    public Task createTask(CreateTaskCommand command) {
        Project project = projectRepository.findById(command.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
        User createdBy = userRepository.findByEmail(command.getCreatedByEmail())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        User assignedTo = userRepository.findById(command.getAssignedToId())
                .orElseThrow(() -> new EntityNotFoundException("Assigned user not found"));

        Task task = new Task();
        task.setProject(project);
        task.setTitle(command.getTitle());
        task.setDescription(command.getDescription());
        task.setStatus(command.getStatus());
        task.setPriority(command.getPriority());
        task.setDueDate(command.getDueDate());
        task.setCreatedBy(createdBy);
        task.setAssignedTo(assignedTo);

        Task savedTask = taskRepository.save(task);
        distributedEventService.record("TASK_CREATED", "taskId=" + savedTask.getId());
        return savedTask;
    }

    @Transactional
    public Task updateTask(UpdateTaskCommand command) {
        Task task = taskRepository.findById(command.getTaskId())
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));

        if (command.getTitle() != null) {
            task.setTitle(command.getTitle());
        }
        if (command.getDescription() != null) {
            task.setDescription(command.getDescription());
        }
        if (command.getStatus() != null) {
            task.setStatus(command.getStatus());
        }
        if (command.getPriority() != null) {
            task.setPriority(command.getPriority());
        }
        if (command.getDueDate() != null) {
            task.setDueDate(command.getDueDate());
        }
        if (command.getAssignedToId() != null) {
            User assignedTo = userRepository.findById(command.getAssignedToId())
                    .orElseThrow(() -> new EntityNotFoundException("Assigned user not found"));
            task.setAssignedTo(assignedTo);
        }

        Task savedTask = taskRepository.save(task);
        distributedEventService.record("TASK_UPDATED", "taskId=" + savedTask.getId());
        return savedTask;
    }

    @Transactional
    public void deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        taskCommentRepository.deleteByTask(task);
        taskRepository.delete(task);
        distributedEventService.record("TASK_DELETED", "taskId=" + taskId);
    }

    @Transactional
    public Task setTaskStatus(SetTaskStatusCommand command) {
        Task task = taskRepository.findById(command.getTaskId())
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        task.setStatus(command.getStatus());

        Task savedTask = taskRepository.save(task);
        distributedEventService.record("TASK_STATUS_CHANGED",
                "taskId=" + savedTask.getId() + ",status=" + command.getStatus());
        return savedTask;
    }

    @Transactional
    public TaskComment addComment(AddTaskCommentCommand command) {
        Task task = taskRepository.findById(command.getTaskId())
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        User user = userRepository.findByEmail(command.getAuthorEmail())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        TaskComment comment = new TaskComment();
        comment.setTask(task);
        comment.setUser(user);
        comment.setComment(command.getComment());

        TaskComment savedComment = taskCommentRepository.save(comment);
        distributedEventService.record("TASK_COMMENT_ADDED",
                "taskId=" + task.getId() + ",commentId=" + savedComment.getId());
        return savedComment;
    }
}
