package com.bsr.messaging;

import com.bsr.config.TaskRabbitMqConfig;
import com.bsr.model.Task;
import com.bsr.model.TaskComment;
import com.bsr.service.TaskCreationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskOperationListener {
    private final TaskCreationService taskCreationService;

    @RabbitListener(
            id = TaskRabbitMqConfig.TASK_UPDATE_LISTENER_ID,
            queues = TaskRabbitMqConfig.TASK_UPDATE_QUEUE,
            autoStartup = "false"
    )
    public TaskOperationResult update(UpdateTaskCommand command) {
        try {
            Task task = taskCreationService.updateTask(command);
            return TaskOperationResult.success(task.getId());
        } catch (Exception exception) {
            return TaskOperationResult.failure(exception.getMessage());
        }
    }

    @RabbitListener(
            id = TaskRabbitMqConfig.TASK_DELETE_LISTENER_ID,
            queues = TaskRabbitMqConfig.TASK_DELETE_QUEUE,
            autoStartup = "false"
    )
    public TaskOperationResult delete(DeleteTaskCommand command) {
        try {
            taskCreationService.deleteTask(command.getTaskId());
            return TaskOperationResult.success(command.getTaskId());
        } catch (Exception exception) {
            return TaskOperationResult.failure(exception.getMessage());
        }
    }

    @RabbitListener(
            id = TaskRabbitMqConfig.TASK_REVIEW_LISTENER_ID,
            queues = TaskRabbitMqConfig.TASK_REVIEW_QUEUE,
            autoStartup = "false"
    )
    public TaskOperationResult setStatus(SetTaskStatusCommand command) {
        try {
            Task task = taskCreationService.setTaskStatus(command);
            return TaskOperationResult.success(task.getId());
        } catch (Exception exception) {
            return TaskOperationResult.failure(exception.getMessage());
        }
    }

    @RabbitListener(
            id = TaskRabbitMqConfig.TASK_COMMENT_LISTENER_ID,
            queues = TaskRabbitMqConfig.TASK_COMMENT_QUEUE,
            autoStartup = "false"
    )
    public TaskOperationResult addComment(AddTaskCommentCommand command) {
        try {
            TaskComment comment = taskCreationService.addComment(command);
            return TaskOperationResult.commentSuccess(command.getTaskId(), comment.getId());
        } catch (Exception exception) {
            return TaskOperationResult.failure(exception.getMessage());
        }
    }
}
