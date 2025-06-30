package pl.projekt.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO do zwracania komentarza do zadania wraz z informacją o autorze.
 */
@Data
public class TaskCommentResponse {
    private Long id;
    private String comment;
    private LocalDateTime createdAt;
    private String authorFirstName;
    private String authorLastName;
    private String authorEmail;
}