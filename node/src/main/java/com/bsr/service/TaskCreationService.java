package com.bsr.service;

import com.bsr.messaging.CreateTaskCommand;
import com.bsr.model.Project;
import com.bsr.model.Task;
import com.bsr.model.User;
import com.bsr.repository.ProjectRepository;
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

        return taskRepository.save(task);
    }
}
