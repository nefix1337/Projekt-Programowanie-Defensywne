package com.bsr.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DistributedEventService {
    private final JdbcTemplate jdbcTemplate;

    @Value("${node.id:node-local}")
    private String nodeId;

    @PostConstruct
    public void initialize() {
        ensureTable();
    }

    public void record(String eventType, String details) {
        ensureTable();
        jdbcTemplate.update("""
                INSERT INTO distributed_node_events (event_time, node_id, event_type, details)
                VALUES (?, ?, ?, ?)
                """, Timestamp.valueOf(LocalDateTime.now()), nodeId, eventType, details);
    }

    private void ensureTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS distributed_node_events (
                    id BIGSERIAL PRIMARY KEY,
                    event_time TIMESTAMP NOT NULL,
                    node_id VARCHAR(128) NOT NULL,
                    event_type VARCHAR(64) NOT NULL,
                    details TEXT
                )
                """);
    }
}
