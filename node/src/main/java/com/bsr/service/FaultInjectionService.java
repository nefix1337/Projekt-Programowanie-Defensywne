package com.bsr.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Sprawdza i stosuje aktywne wstrzyknięcia awarii (opóźnienie sieciowe, uszkodzenie wiadomości)
 * skonfigurowane dla tego węzła w tabeli {@code node_leader_candidates}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FaultInjectionService {
    private final JdbcTemplate jdbcTemplate;
    private final DistributedEventService distributedEventService;

    @Value("${node.id:node-local}")
    private String nodeId;

    public void applyFaults(String operation) {
        FaultState state = readState();

        if (state.networkDelayMs() > 0) {
            log.warn("Node {} simulating network delay of {} ms before processing {}",
                    nodeId, state.networkDelayMs(), operation);
            distributedEventService.record("NETWORK_DELAY_APPLIED",
                    "operation=" + operation + ",delayMs=" + state.networkDelayMs());
            sleep(state.networkDelayMs());
        }

        if (state.messageCorruption()) {
            log.error("Node {} simulating message corruption while processing {}", nodeId, operation);
            distributedEventService.record("MESSAGE_CORRUPTION_TRIGGERED", "operation=" + operation);
            throw new IllegalStateException("Simulated message corruption while processing: " + operation);
        }
    }

    private FaultState readState() {
        return jdbcTemplate.query("""
                SELECT network_delay_ms, message_corruption
                FROM node_leader_candidates
                WHERE node_id = ?
                """, resultSet -> resultSet.next()
                        ? new FaultState(resultSet.getInt("network_delay_ms"), resultSet.getBoolean("message_corruption"))
                        : new FaultState(0, false),
                nodeId);
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private record FaultState(int networkDelayMs, boolean messageCorruption) {
    }
}
