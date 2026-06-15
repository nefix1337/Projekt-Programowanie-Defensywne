package com.bsr.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;

@DisplayName("Testy serwisu DistributedEventService")
@ExtendWith(MockitoExtension.class)
class DistributedEventServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private DistributedEventService distributedEventService;

    @BeforeEach
    void setUp() {
        distributedEventService = new DistributedEventService(jdbcTemplate);
        ReflectionTestUtils.setField(distributedEventService, "nodeId", "node-2");
    }

    @Test
    @DisplayName("Inicjalizacja tworzy tabelę zdarzeń, jeśli nie istnieje")
    void initialize_CreatesEventsTableIfMissing() {
        distributedEventService.initialize();

        verify(jdbcTemplate).execute(contains("CREATE TABLE IF NOT EXISTS distributed_node_events"));
    }

    @Test
    @DisplayName("Rejestrowanie zdarzenia zapewnia istnienie tabeli przed wstawieniem")
    void record_EnsuresTableExistsBeforeInserting() {
        distributedEventService.record("NODE_RECOVERED", "details");

        verify(jdbcTemplate).execute(contains("CREATE TABLE IF NOT EXISTS distributed_node_events"));
        verify(jdbcTemplate).update(contains("INSERT INTO distributed_node_events"), any(Object[].class));
    }

    @Test
    @DisplayName("Rejestrowanie zdarzenia zapisuje znacznik czasu, identyfikator węzła, typ zdarzenia i szczegóły")
    void record_InsertsEventWithTimestampNodeIdEventTypeAndDetails() {
        distributedEventService.record("TASK_CREATED", "taskId=42");

        ArgumentCaptor<Object[]> argsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(jdbcTemplate).update(contains("INSERT INTO distributed_node_events"), argsCaptor.capture());

        Object[] args = argsCaptor.getValue();
        assertEquals(4, args.length);
        assertInstanceOf(Timestamp.class, args[0]);
        assertEquals("node-2", args[1]);
        assertEquals("TASK_CREATED", args[2]);
        assertEquals("taskId=42", args[3]);
    }
}
