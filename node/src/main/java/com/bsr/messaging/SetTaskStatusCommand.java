package com.bsr.messaging;

import com.bsr.model.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetTaskStatusCommand {
    private Long taskId;
    private TaskStatus status;
}
