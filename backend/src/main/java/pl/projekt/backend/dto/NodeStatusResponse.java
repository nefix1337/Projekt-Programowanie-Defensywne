package pl.projekt.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class NodeStatusResponse {
    private String nodeId;
    private int weight;
    private boolean online;
    private boolean leader;
    private boolean forcedDown;
    private LocalDateTime lastSeen;
    private Long secondsSinceLastSeen;
}
