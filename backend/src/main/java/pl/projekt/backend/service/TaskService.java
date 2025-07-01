package pl.projekt.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;
import pl.projekt.backend.model.*;
import pl.projekt.backend.repository.*;
import pl.projekt.backend.dto.CreateTaskRequest;
import pl.projekt.backend.dto.UpdateTaskRequest;
import pl.projekt.backend.dto.TaskWithAssigneeResponse;
import pl.projekt.backend.dto.TaskAssigneeDetailsResponse;
import pl.projekt.backend.dto.TaskCreatorDetailsResponse;
import pl.projekt.backend.dto.AddTaskCommentRequest;
import pl.projekt.backend.dto.TaskCommentResponse;

import java.util.List;
import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskCommentRepository taskCommentRepository;

    public Task createTask(CreateTaskRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));
        String currentUserEmail = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User createdBy = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User assignedTo = userRepository.findById(request.getAssignedToId())
                .orElseThrow(() -> new RuntimeException("Assigned user not found"));

        Task task = new Task();
        task.setProject(project);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());
        task.setCreatedBy(createdBy);
        task.setAssignedTo(assignedTo);

        return taskRepository.save(task);
    }

    public Task updateTask(Long id, UpdateTaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getDueDate() != null) task.setDueDate(request.getDueDate());
        if (request.getAssignedToId() != null) {
            User assignedTo = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new RuntimeException("Assigned user not found"));
            task.setAssignedTo(assignedTo);
        }

        return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    // Zadania przypisane do użytkownika (wszystkie projekty)
    public List<Task> getTasksForUser(String username) {
        User user = userRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return taskRepository.findByAssignedTo(user);
    }

    // Wszystkie zadania w projekcie (dla MANAGERA)
    public List<Task> getAllTasksForProject(UUID projectId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new EntityNotFoundException("Project not found"));
        return taskRepository.findByProject(project);
    }

    // Zadania w projekcie przypisane do zalogowanego użytkownika
    public List<Task> getTasksByProject(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        String currentUserEmail = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return taskRepository.findByProjectAndAssignedTo(project, user);
    }

    public List<TaskWithAssigneeResponse> getAllTasksForProjectWithAssignee(UUID projectId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new EntityNotFoundException("Project not found"));
        return taskRepository.findByProject(project).stream()
            .map(task -> new TaskWithAssigneeResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus() != null ? task.getStatus().name() : null,
                task.getPriority() != null ? task.getPriority().name() : null,
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getDueDate(),
                task.getAssignedTo() != null ? task.getAssignedTo().getFirstName() : null,
                task.getAssignedTo() != null ? task.getAssignedTo().getLastName() : null
            ))
            .toList();
    }

    public TaskWithAssigneeResponse getTaskWithAssigneeById(Long id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        return new TaskWithAssigneeResponse(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus() != null ? task.getStatus().name() : null,
            task.getPriority() != null ? task.getPriority().name() : null,
            task.getCreatedAt(),
            task.getUpdatedAt(),
            task.getDueDate(),
            task.getAssignedTo() != null ? task.getAssignedTo().getFirstName() : null,
            task.getAssignedTo() != null ? task.getAssignedTo().getLastName() : null
        );
    }

    public TaskAssigneeDetailsResponse getTaskAssigneeDetailsById(Long id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        return new TaskAssigneeDetailsResponse(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus() != null ? task.getStatus().name() : null,
            task.getPriority() != null ? task.getPriority().name() : null,
            task.getCreatedAt(),
            task.getUpdatedAt(),
            task.getDueDate(),
            task.getAssignedTo() != null ? task.getAssignedTo().getFirstName() : null,
            task.getAssignedTo() != null ? task.getAssignedTo().getLastName() : null,
            task.getAssignedTo() != null ? task.getAssignedTo().getEmail() : null
        );
    }

    public TaskCreatorDetailsResponse getTaskCreatorDetailsById(Long id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        return new TaskCreatorDetailsResponse(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus() != null ? task.getStatus().name() : null,
            task.getPriority() != null ? task.getPriority().name() : null,
            task.getCreatedAt(),
            task.getUpdatedAt(),
            task.getDueDate(),
            task.getCreatedBy() != null ? task.getCreatedBy().getFirstName() : null,
            task.getCreatedBy() != null ? task.getCreatedBy().getLastName() : null,
            task.getCreatedBy() != null ? task.getCreatedBy().getEmail() : null
        );
    }

    public TaskWithAssigneeResponse setTaskStatusToReview(Long id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        task.setStatus(TaskStatus.TO_REVIEW);
        taskRepository.save(task);
        User assigned = task.getAssignedTo();
        return new TaskWithAssigneeResponse(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus().name(),
            task.getPriority() != null ? task.getPriority().name() : null,
            task.getCreatedAt(),
            task.getUpdatedAt(),
            task.getDueDate(),
            assigned != null ? assigned.getFirstName() : null,
            assigned != null ? assigned.getLastName() : null
        );
    }

    /**
     * Dodaje komentarz do zadania.
     * @param taskId id zadania
     * @param request treść komentarza
     * @return dodany komentarz
     */
    public TaskComment addCommentToTask(Long taskId, AddTaskCommentRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TaskComment comment = new TaskComment();
        comment.setTask(task);
        comment.setUser(user);
        comment.setComment(request.getComment());

        return taskCommentRepository.save(comment);
    }

    /**
     * Pobiera komentarze do zadania.
     */
    public List<TaskCommentResponse> getCommentsForTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        List<TaskComment> comments = taskCommentRepository.findByTask(task);

        return comments.stream().map(comment -> {
            TaskCommentResponse dto = new TaskCommentResponse();
            dto.setId(comment.getId());
            dto.setComment(comment.getComment());
            dto.setCreatedAt(comment.getCreatedAt());
            dto.setAuthorFirstName(comment.getUser().getFirstName());
            dto.setAuthorLastName(comment.getUser().getLastName());
            dto.setAuthorEmail(comment.getUser().getEmail());
            return dto;
        }).toList();
    }

    public List<TaskWithAssigneeResponse> getTasksForUserWithAssignee(String username) {
        User user = userRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return taskRepository.findByAssignedTo(user).stream()
            .map(task -> new TaskWithAssigneeResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus() != null ? task.getStatus().name() : null,
                task.getPriority() != null ? task.getPriority().name() : null,
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getDueDate(),
                task.getAssignedTo() != null ? task.getAssignedTo().getFirstName() : null,
                task.getAssignedTo() != null ? task.getAssignedTo().getLastName() : null
            ))
            .toList();
    }
}