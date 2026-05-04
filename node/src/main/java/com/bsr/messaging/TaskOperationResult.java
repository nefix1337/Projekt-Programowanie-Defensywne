package com.bsr.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskOperationResult {
    private boolean success;
    private Long taskId;
    private Long commentId;
    private String errorMessage;

    public static TaskOperationResult success(Long taskId) {
        return new TaskOperationResult(true, taskId, null, null);
    }

    public static TaskOperationResult commentSuccess(Long taskId, Long commentId) {
        return new TaskOperationResult(true, taskId, commentId, null);
    }

    public static TaskOperationResult failure(String errorMessage) {
        return new TaskOperationResult(false, null, null, errorMessage);
    }
}
