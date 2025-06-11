package pl.projekt.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.projekt.backend.model.Project;
import pl.projekt.backend.model.Task;
import pl.projekt.backend.model.User;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProject(Project project);
    List<Task> findByAssignedTo(User user);
    List<Task> findByProjectAndAssignedTo(Project project, User user);
}