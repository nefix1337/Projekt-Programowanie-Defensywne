package com.bsr.service;

import com.bsr.config.TaskRabbitMqConfig;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class LeaderElectionService {
    private final JdbcTemplate jdbcTemplate;
    private final RabbitListenerEndpointRegistry listenerRegistry;
    private final String nodeId;
    private final int nodeWeight;
    private final long ttlSeconds;
    private final AtomicBoolean leader = new AtomicBoolean(false);

    public LeaderElectionService(
            JdbcTemplate jdbcTemplate,
            RabbitListenerEndpointRegistry listenerRegistry,
            @Value("${node.id:node-local}") String nodeId,
            @Value("${node.weight:0}") int nodeWeight,
            @Value("${node.leader.ttl-seconds:15}") long ttlSeconds) {
        this.jdbcTemplate = jdbcTemplate;
        this.listenerRegistry = listenerRegistry;
        this.nodeId = nodeId;
        this.nodeWeight = nodeWeight;
        this.ttlSeconds = ttlSeconds;
    }

    @PostConstruct
    public void initialize() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS node_leader_candidates (
                    node_id VARCHAR(128) PRIMARY KEY,
                    node_weight INTEGER NOT NULL,
                    last_seen TIMESTAMP NOT NULL,
                    forced_down BOOLEAN NOT NULL DEFAULT FALSE
                )
                """);
        jdbcTemplate.execute("""
                ALTER TABLE node_leader_candidates
                ADD COLUMN IF NOT EXISTS forced_down BOOLEAN NOT NULL DEFAULT FALSE
                """);
        log.info("Node {} registered for leader election with weight {}", nodeId, nodeWeight);
    }

    @Scheduled(
            initialDelayString = "${node.leader.initial-delay-ms:1000}",
            fixedDelayString = "${node.leader.election-interval-ms:5000}"
    )
    public void updateLeadership() {
        if (isForcedDown()) {
            if (leader.compareAndSet(true, false)) {
                stopTaskListener();
                log.info("Node {} stopped leadership because fault injection is active", nodeId);
            }
            return;
        }

        sendHeartbeat();

        Optional<String> currentLeader = findCurrentLeader();
        boolean shouldBeLeader = currentLeader.map(nodeId::equals).orElse(false);

        if (shouldBeLeader && leader.compareAndSet(false, true)) {
            startTaskListener();
            log.info("Node {} became leader with weight {}", nodeId, nodeWeight);
        } else if (!shouldBeLeader && leader.compareAndSet(true, false)) {
            stopTaskListener();
            log.info("Node {} stepped down. Current leader is {}", nodeId, currentLeader.orElse("none"));
        }
    }

    @PreDestroy
    public void unregister() {
        stopTaskListener();
        jdbcTemplate.update("DELETE FROM node_leader_candidates WHERE node_id = ?", nodeId);
    }

    private void sendHeartbeat() {
        jdbcTemplate.update("""
                INSERT INTO node_leader_candidates (node_id, node_weight, last_seen)
                VALUES (?, ?, ?)
                ON CONFLICT (node_id)
                DO UPDATE SET node_weight = EXCLUDED.node_weight, last_seen = EXCLUDED.last_seen
                """, nodeId, nodeWeight, Timestamp.valueOf(LocalDateTime.now()));
    }

    private boolean isForcedDown() {
        return jdbcTemplate.query("""
                SELECT forced_down
                FROM node_leader_candidates
                WHERE node_id = ?
                """, resultSet -> resultSet.next() && resultSet.getBoolean("forced_down"), nodeId);
    }

    private Optional<String> findCurrentLeader() {
        LocalDateTime aliveSince = LocalDateTime.now().minusSeconds(ttlSeconds);
        return jdbcTemplate.query("""
                SELECT node_id
                FROM node_leader_candidates
                WHERE last_seen >= ? AND forced_down = FALSE
                ORDER BY node_weight DESC, node_id ASC
                LIMIT 1
                """, resultSet -> {
                    if (resultSet.next()) {
                        return Optional.of(resultSet.getString("node_id"));
                    }
                    return Optional.empty();
                }, Timestamp.valueOf(aliveSince));
    }

    private void startTaskListener() {
        MessageListenerContainer container = listenerRegistry.getListenerContainer(TaskRabbitMqConfig.TASK_CREATE_LISTENER_ID);
        if (container != null && !container.isRunning()) {
            container.start();
        }
    }

    private void stopTaskListener() {
        MessageListenerContainer container = listenerRegistry.getListenerContainer(TaskRabbitMqConfig.TASK_CREATE_LISTENER_ID);
        if (container != null && container.isRunning()) {
            container.stop();
        }
    }
}
