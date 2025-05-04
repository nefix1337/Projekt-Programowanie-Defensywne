package pl.projekt.backend.dto;

import lombok.Data;

@Data
public class TotpRequest {
    private String totpCode;
    private String email;
    
}
