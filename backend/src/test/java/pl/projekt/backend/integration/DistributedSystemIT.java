package pl.projekt.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Testy integracyjne uruchamiane na zywym stosie docker-compose
 * (backend + node-1/2/3 + RabbitMQ + Postgres pod adresem {@code integration.baseUrl}, domyslnie http://localhost:8080).
 * <p>
 * Wymagaja uruchomionego srodowiska: {@code docker compose up -d --build}.
 * Klasa nazwana z sufiksem "IT" - domyslny {@code mvn test} jej nie uruchamia
 * (nie jest dopasowywana przez wzorce surefire), uruchamiane przez profil
 * {@code mvn test -Pintegration-tests}.
 */
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DistributedSystemIT {

    private static final String BASE_URL = System.getProperty("integration.baseUrl", "http://localhost:8080");
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final ObjectMapper JSON = new ObjectMapper();

    private static final String TEST_MANAGER_EMAIL = "integration-tester@example.com";
    private static final String TEST_MANAGER_PASSWORD = "integration123";
    private static final String TEST_PROJECT_NAME = "Integration Test Project";

    private static String adminToken;
    private static String managerToken;
    private static long assigneeUserId;
    private static String projectId;
    private static long taskId;

    private static String failedNodeId;

    @BeforeAll
    static void setUp() throws Exception {
        adminToken = login("admin@example.com", "admin123");
        managerToken = ensureTestManager();
        assigneeUserId = findUserIdByEmail("developer@example.com");
        projectId = ensureTestProject();
    }

    @AfterAll
    static void tearDown() throws Exception {
        if (taskId != 0) {
            delete(BASE_URL + "/api/tasks/" + taskId, managerToken);
        }
        for (String nodeId : new String[] {"node-1", "node-2", "node-3"}) {
            post(BASE_URL + "/api/admin/nodes/" + nodeId + "/recovery", adminToken, null);
            post(BASE_URL + "/api/admin/nodes/" + nodeId + "/network-delay", adminToken, "{\"delayMs\":0}");
            post(BASE_URL + "/api/admin/nodes/" + nodeId + "/message-corruption/clear", adminToken, null);
        }
    }

    @Test
    @Order(1)
    void nodesAreOnlineWithExactlyOneLeader() throws Exception {
        JsonNode nodes = getJson("/api/admin/nodes", adminToken);
        assertEquals(3, nodes.size(), "Expected 3 registered nodes");

        long leaderCount = 0;
        for (JsonNode node : nodes) {
            assertTrue(node.get("online").asBoolean(), "Node " + node.get("nodeId") + " should be online");
            if (node.get("leader").asBoolean()) {
                leaderCount++;
            }
        }
        assertEquals(1, leaderCount, "Exactly one node should be the leader");
    }

    @Test
    @Order(2)
    void managerCanCreateTaskViaRabbitMqLeader() throws Exception {
        String body = """
                {
                  "projectId": "%s",
                  "title": "Integration test task",
                  "description": "Created by DistributedSystemIntegrationTest",
                  "status": "TODO",
                  "priority": "MEDIUM",
                  "assignedToId": %d
                }
                """.formatted(projectId, assigneeUserId);

        HttpResponse<String> response = post(BASE_URL + "/api/tasks", managerToken, body);
        assertEquals(200, response.statusCode(), response.body());

        JsonNode task = JSON.readTree(response.body());
        taskId = task.get("id").asLong();
        assertEquals("Integration test task", task.get("title").asText());
        assertEquals("TODO", task.get("status").asText());
    }

    @Test
    @Order(3)
    void networkDelayAboveReplyTimeoutCauses503() throws Exception {
        String leader = currentLeaderNodeId();

        setNetworkDelay(leader, 11000);
        try {
            HttpResponse<String> response = updateTask(taskId, "Delayed update", "TODO");
            assertEquals(503, response.statusCode(), response.body());

            JsonNode error = JSON.readTree(response.body());
            assertEquals(503, error.get("status").asInt());
        } finally {
            setNetworkDelay(leader, 0);
            // wait for the leader to finish processing the delayed message before the next test
            Thread.sleep(2000);
        }
    }

    @Test
    @Order(4)
    void messageCorruptionCauses500() throws Exception {
        String leader = currentLeaderNodeId();

        assertEquals(204, post(BASE_URL + "/api/admin/nodes/" + leader + "/message-corruption", adminToken, null).statusCode());
        try {
            HttpResponse<String> response = updateTask(taskId, "Corrupted update", "TODO");
            assertEquals(500, response.statusCode(), response.body());

            JsonNode error = JSON.readTree(response.body());
            assertTrue(error.get("message").asText().toLowerCase().contains("simulated"),
                    "Expected simulated corruption message, got: " + error.get("message"));
        } finally {
            post(BASE_URL + "/api/admin/nodes/" + leader + "/message-corruption/clear", adminToken, null);
        }
    }

    @Test
    @Order(5)
    void nodeFailureTriggersLeaderFailover() throws Exception {
        failedNodeId = currentLeaderNodeId();

        assertEquals(204, post(BASE_URL + "/api/admin/nodes/" + failedNodeId + "/failure", adminToken, null).statusCode());

        String newLeader = awaitNewLeader(failedNodeId, Duration.ofSeconds(20));
        assertNotEquals(failedNodeId, newLeader, "A different node should take over leadership");

        JsonNode nodes = getJson("/api/admin/nodes", adminToken);
        for (JsonNode node : nodes) {
            if (node.get("nodeId").asText().equals(failedNodeId)) {
                assertTrue(node.get("forcedDown").asBoolean());
                assertFalse(node.get("online").asBoolean());
                assertFalse(node.get("leader").asBoolean());
            }
        }
    }

    @Test
    @Order(6)
    void taskOperationsSucceedAfterFailover() throws Exception {
        HttpResponse<String> response = updateTask(taskId, "Updated after failover", "DONE");
        assertEquals(200, response.statusCode(), response.body());

        JsonNode task = JSON.readTree(response.body());
        assertEquals("Updated after failover", task.get("title").asText());
        assertEquals("DONE", task.get("status").asText());

        assertEquals(204, post(BASE_URL + "/api/admin/nodes/" + failedNodeId + "/recovery", adminToken, null).statusCode());
    }

    @Test
    @Order(7)
    void eventHistoryAndMetricsReflectFaultInjection() throws Exception {
        JsonNode events = getJson("/api/admin/nodes/events", adminToken);
        assertTrue(events.size() > 0, "Event history should not be empty");

        Set<String> eventTypes = new HashSet<>();
        for (JsonNode event : events) {
            eventTypes.add(event.get("eventType").asText());
        }

        assertTrue(eventTypes.contains("NODE_FAILURE_INJECTED"), eventTypes::toString);
        assertTrue(eventTypes.contains("NODE_RECOVERED"), eventTypes::toString);
        assertTrue(eventTypes.contains("NETWORK_DELAY_INJECTED"), eventTypes::toString);
        assertTrue(eventTypes.contains("NETWORK_DELAY_APPLIED"), eventTypes::toString);
        assertTrue(eventTypes.contains("MESSAGE_CORRUPTION_INJECTED"), eventTypes::toString);
        assertTrue(eventTypes.contains("MESSAGE_CORRUPTION_TRIGGERED"), eventTypes::toString);
        assertTrue(eventTypes.contains("TASK_UPDATED"), eventTypes::toString);

        JsonNode metrics = getJson("/api/admin/nodes/metrics", adminToken);
        assertTrue(metrics.size() > 0, "Metrics should not be empty");
        for (JsonNode metric : metrics) {
            assertTrue(metric.get("count").asLong() > 0);
        }
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private static String currentLeaderNodeId() throws Exception {
        JsonNode nodes = getJson("/api/admin/nodes", adminToken);
        for (JsonNode node : nodes) {
            if (node.get("leader").asBoolean()) {
                return node.get("nodeId").asText();
            }
        }
        fail("No leader elected");
        return null;
    }

    private static String awaitNewLeader(String oldLeaderId, Duration timeout) throws Exception {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            JsonNode nodes = getJson("/api/admin/nodes", adminToken);
            for (JsonNode node : nodes) {
                if (node.get("leader").asBoolean() && !node.get("nodeId").asText().equals(oldLeaderId)) {
                    return node.get("nodeId").asText();
                }
            }
            Thread.sleep(1000);
        }
        fail("No failover leader elected within " + timeout);
        return null;
    }

    private static void setNetworkDelay(String nodeId, int delayMs) throws Exception {
        HttpResponse<String> response = post(BASE_URL + "/api/admin/nodes/" + nodeId + "/network-delay",
                adminToken, "{\"delayMs\":" + delayMs + "}");
        assertEquals(204, response.statusCode(), response.body());
    }

    private static HttpResponse<String> updateTask(long id, String title, String status) throws Exception {
        String body = """
                {
                  "title": "%s",
                  "description": "Updated by DistributedSystemIntegrationTest",
                  "status": "%s",
                  "priority": "HIGH",
                  "assignedToId": %d
                }
                """.formatted(title, status, assigneeUserId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/tasks/" + id))
                .header("Authorization", "Bearer " + managerToken)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(20))
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return HTTP.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static String ensureTestManager() throws Exception {
        String token = tryLogin(TEST_MANAGER_EMAIL, TEST_MANAGER_PASSWORD);
        if (token != null) {
            return token;
        }

        String registerBody = """
                {
                  "firstName": "Integration",
                  "lastName": "Tester",
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(TEST_MANAGER_EMAIL, TEST_MANAGER_PASSWORD);
        HttpResponse<String> registerResponse = post(BASE_URL + "/api/auth/register", null, registerBody);
        assertEquals(200, registerResponse.statusCode(), registerResponse.body());

        String changeRoleBody = """
                {
                  "email": "%s",
                  "newRole": "MANAGER"
                }
                """.formatted(TEST_MANAGER_EMAIL);
        HttpResponse<String> roleResponse = post(BASE_URL + "/api/admin/change-role", adminToken, changeRoleBody);
        assertEquals(200, roleResponse.statusCode(), roleResponse.body());

        token = tryLogin(TEST_MANAGER_EMAIL, TEST_MANAGER_PASSWORD);
        assertNotNull(token, "Test manager login must succeed after role change");
        return token;
    }

    private static String ensureTestProject() throws Exception {
        JsonNode projects = getJson("/api/projects", managerToken);
        for (JsonNode project : projects) {
            if (TEST_PROJECT_NAME.equals(project.get("name").asText())) {
                return project.get("id").asText();
            }
        }

        String body = """
                {
                  "name": "%s",
                  "description": "Project used by distributed system integration tests",
                  "status": "IN_PROGRESS"
                }
                """.formatted(TEST_PROJECT_NAME);
        HttpResponse<String> response = post(BASE_URL + "/api/projects", managerToken, body);
        assertEquals(200, response.statusCode(), response.body());
        return JSON.readTree(response.body()).get("id").asText();
    }

    private static long findUserIdByEmail(String email) throws Exception {
        JsonNode users = getJson("/api/admin/users", adminToken);
        for (JsonNode user : users) {
            if (email.equals(user.get("email").asText())) {
                return user.get("id").asLong();
            }
        }
        fail("User not found: " + email);
        return -1;
    }

    private static String login(String email, String password) throws Exception {
        String token = tryLogin(email, password);
        assertNotNull(token, "Login failed for " + email);
        return token;
    }

    private static String tryLogin(String email, String password) throws Exception {
        String body = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);
        HttpResponse<String> response = post(BASE_URL + "/api/auth/login", null, body);
        if (response.statusCode() != 200) {
            return null;
        }
        JsonNode json = JSON.readTree(response.body());
        JsonNode tokenNode = json.get("token");
        return (tokenNode != null && !tokenNode.isNull()) ? tokenNode.asText() : null;
    }

    private static JsonNode getJson(String path, String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Authorization", "Bearer " + token)
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), response.body());
        return JSON.readTree(response.body());
    }

    private static HttpResponse<String> post(String url, String token, String body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        builder.POST(body != null
                ? HttpRequest.BodyPublishers.ofString(body)
                : HttpRequest.BodyPublishers.noBody());
        return HTTP.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> delete(String url, String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .timeout(Duration.ofSeconds(10))
                .DELETE()
                .build();
        return HTTP.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
