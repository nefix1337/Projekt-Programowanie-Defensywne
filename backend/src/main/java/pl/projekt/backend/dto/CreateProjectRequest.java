package pl.projekt.backend.dto;

import lombok.Data;

@Data
public class CreateProjectRequest {
    private String name;
    private String description;
    private String status; // Możemy użyć String, aby przesyłać status jako tekst
}