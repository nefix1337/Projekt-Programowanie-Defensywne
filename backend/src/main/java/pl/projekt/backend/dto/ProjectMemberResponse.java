package pl.projekt.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.projekt.backend.model.ProjectRole;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectMemberResponse {
    private Long id;
    private Long userId;
    private String userEmail;
    private String firstName;
    private String lastName;
    private ProjectRole projectRole;
    private LocalDateTime joinedAt;
}