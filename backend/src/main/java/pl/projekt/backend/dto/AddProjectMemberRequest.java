package pl.projekt.backend.dto;

import lombok.Data;
import pl.projekt.backend.model.ProjectRole;

@Data
public class AddProjectMemberRequest {
    private String userEmail;
    private ProjectRole projectRole;
}