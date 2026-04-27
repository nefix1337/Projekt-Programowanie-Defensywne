package com.bsr.messaging;

import com.bsr.config.TaskRabbitMqConfig;
import com.bsr.model.Task;
import com.bsr.service.TaskCreationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskCreationListener {
    private final TaskCreationService taskCreationService;

    @RabbitListener(queues = TaskRabbitMqConfig.TASK_CREATE_QUEUE)
    public CreateTaskResult handle(CreateTaskCommand command) {
        try {
            Task task = taskCreationService.createTask(command);
            return CreateTaskResult.success(task.getId());
        } catch (Exception exception) {
            return CreateTaskResult.failure(exception.getMessage());
        }
    }
}
