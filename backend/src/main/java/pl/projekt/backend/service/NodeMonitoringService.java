package pl.projekt.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import pl.projekt.backend.dto.NodeStatusResponse;

import jakarta.annotation.PostConstruct;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NodeMonitoringService {
    private final JdbcTemplate jdbcTemplate;

    @Value("${node.monitoring.expected-nodes:node-1:100,node-2:50,node-3:10}")
    private String expectedNodes;

    @Value("${node.monitoring.ttl-seconds:15}")
    private long ttlSeconds;

    @PostConstruct
    public void initialize() {
        ensureTable();
    }

    public List<NodeStatusResponse> getStatuses() {
        ensureTable();
        Map<String, Integer> expected = parseExpectedNodes();
        Map<String, CandidateRow> rows = findRows();
        Optional<String> leaderId = findLeaderId();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime aliveSince = now.minusSeconds(ttlSeconds);

        return expected.entrySet().stream()
                .map(entry -> {
                    CandidateRow row = rows.get(entry.getKey());
                    int weight = row != null ? row.weight() : entry.getValue();
                    boolean forcedDown = row != null && row.forcedDown();
                    LocalDateTime lastSeen = row != null ? row.lastSeen() : null;
                    boolean online = lastSeen != null && !lastSeen.isBefore(aliveSince) && !forcedDown;
                    Long secondsSinceLastSeen = lastSeen != null ? Duration.between(lastSeen, now).getSeconds() : null;
                    boolean leader = online && leaderId.map(entry.getKey()::equals).orElse(false);
                    return new NodeStatusResponse(
                            entry.getKey(),
                            weight,
                            online,
                            leader,
                            forcedDown,
                            lastSeen,
                            secondsSinceLastSeen
                    );
                })
                .toList();
    }

    public void injectFailure(String nodeId) {
        setForcedDown(nodeId, true);
    }

    public void recover(String nodeId) {
        setForcedDown(nodeId, false);
    }

    private void setForcedDown(String nodeId, boolean forcedDown) {
        ensureTable();
        int weight = parseExpectedNodes().getOrDefault(nodeId, 0);
        jdbcTemplate.update("""
                INSERT INTO node_leader_candidates (node_id, node_weight, last_seen, forced_down)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (node_id)
                DO UPDATE SET forced_down = EXCLUDED.forced_down
                """, nodeId, weight, Timestamp.valueOf(LocalDateTime.now().minusSeconds(ttlSeconds + 1)), forcedDown);
    }

    private Map<String, CandidateRow> findRows() {
        return jdbcTemplate.query("""
                SELECT node_id, node_weight, last_seen, forced_down
                FROM node_leader_candidates
                """, resultSet -> {
            Map<String, CandidateRow> rows = new LinkedHashMap<>();
            while (resultSet.next()) {
                rows.put(resultSet.getString("node_id"), new CandidateRow(
                        resultSet.getInt("node_weight"),
                        resultSet.getTimestamp("last_seen").toLocalDateTime(),
                        resultSet.getBoolean("forced_down")
                ));
            }
            return rows;
        });
    }

    private Optional<String> findLeaderId() {
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

    private Map<String, Integer> parseExpectedNodes() {
        Map<String, Integer> nodes = new LinkedHashMap<>();
        for (String entry : expectedNodes.split(",")) {
            String[] parts = entry.trim().split(":");
            if (parts.length == 2) {
                nodes.put(parts[0], Integer.parseInt(parts[1]));
            }
        }
        return nodes;
    }

    private void ensureTable() {
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
    }

    private record CandidateRow(int weight, LocalDateTime lastSeen, boolean forcedDown) {
    }
}
