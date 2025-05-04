package pl.projekt.backend.dto;

import lombok.Data;

@Data
public class ChangeRoleRequest {
    private String email;
    private String newRole;
}