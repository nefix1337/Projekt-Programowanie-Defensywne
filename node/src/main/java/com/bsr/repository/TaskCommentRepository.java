package com.bsr.repository;

import com.bsr.model.Task;
import com.bsr.model.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {
    void deleteByTask(Task task);
}
