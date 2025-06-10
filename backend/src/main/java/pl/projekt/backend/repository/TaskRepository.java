package pl.projekt.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.projekt.backend.model.Project;
import pl.projekt.backend.model.Task;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProject(Project project);
}