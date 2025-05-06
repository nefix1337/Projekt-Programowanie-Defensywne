package pl.projekt.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserBasicInfo {
    private String firstName;
    private String lastName;
    private String email;
}