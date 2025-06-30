package pl.projekt.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.projekt.backend.model.TaskComment;
import pl.projekt.backend.model.Task;

import java.util.List;

public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {
    List<TaskComment> findByTask(Task task);
}