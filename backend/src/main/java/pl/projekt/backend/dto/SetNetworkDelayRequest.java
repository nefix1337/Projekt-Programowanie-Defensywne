package pl.projekt.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class SetNetworkDelayRequest {

    @Min(value = 0, message = "Opóźnienie nie może być ujemne")
    @Max(value = 30000, message = "Opóźnienie może wynosić maksymalnie 30000 ms")
    private int delayMs;
}
