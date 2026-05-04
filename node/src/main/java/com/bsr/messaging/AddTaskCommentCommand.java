package com.bsr.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddTaskCommentCommand {
    private Long taskId;
    private String comment;
    private String authorEmail;
}
