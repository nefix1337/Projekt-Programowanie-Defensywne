package com.bsr.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskResult {
    private boolean success;
    private Long taskId;
    private String errorMessage;

    public static CreateTaskResult success(Long taskId) {
        return new CreateTaskResult(true, taskId, null);
    }

    public static CreateTaskResult failure(String errorMessage) {
        return new CreateTaskResult(false, null, errorMessage);
    }
}
