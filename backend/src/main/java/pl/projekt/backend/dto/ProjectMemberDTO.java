package pl.projekt.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import pl.projekt.backend.model.ProjectRole;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ProjectMemberDTO {
    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private ProjectRole projectRole;
    private LocalDateTime joinedAt;
}