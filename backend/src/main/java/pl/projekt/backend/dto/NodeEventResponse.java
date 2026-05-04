package pl.projekt.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class NodeEventResponse {
    private Long id;
    private LocalDateTime eventTime;
    private String nodeId;
    private String eventType;
    private String details;
}
