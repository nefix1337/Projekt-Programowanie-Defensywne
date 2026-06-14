package pl.projekt.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventTypeCountResponse {
    private String eventType;
    private long count;
}
