package com.bsr.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FaultInjectionServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private DistributedEventService distributedEventService;

    @Mock
    private ResultSet resultSet;

    private FaultInjectionService faultInjectionService;

    @BeforeEach
    void setUp() {
        faultInjectionService = new FaultInjectionService(jdbcTemplate, distributedEventService);
        ReflectionTestUtils.setField(faultInjectionService, "nodeId", "node-1");
    }

    /**
     * Stubuje odczyt stanu awarii tak, aby rzeczywisty {@link ResultSetExtractor}
     * uzyty w {@code FaultInjectionService.readState()} zostal wywolany na mockowanym
     * {@link ResultSet} - dzieki temu prywatny rekord FaultState jest tworzony
     * przez kod produkcyjny, a nie przez test.
     */
    private void stubFaultState(int networkDelayMs, boolean messageCorruption) throws SQLException {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("network_delay_ms")).thenReturn(networkDelayMs);
        when(resultSet.getBoolean("message_corruption")).thenReturn(messageCorruption);
        when(jdbcTemplate.query(anyString(), ArgumentMatchers.<ResultSetExtractor<Object>>any(), any()))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<?> extractor = invocation.getArgument(1);
                    return extractor.extractData(resultSet);
                });
    }

    @Test
    void applyFaults_DoesNothing_WhenNoFaultsConfigured() throws SQLException {
        stubFaultState(0, false);

        assertDoesNotThrow(() -> faultInjectionService.applyFaults("CREATE_TASK"));

        verify(distributedEventService, never()).record(anyString(), anyString());
    }

    @Test
    void applyFaults_SleepsAndRecordsEvent_WhenNetworkDelayConfigured() throws SQLException {
        stubFaultState(50, false);

        long start = System.currentTimeMillis();
        assertDoesNotThrow(() -> faultInjectionService.applyFaults("UPDATE_TASK"));
        long elapsed = System.currentTimeMillis() - start;

        assertTrue(elapsed >= 50, "Expected applyFaults to block for at least the configured delay");
        verify(distributedEventService).record("NETWORK_DELAY_APPLIED", "operation=UPDATE_TASK,delayMs=50");
    }

    @Test
    void applyFaults_ThrowsAndRecordsEvent_WhenMessageCorruptionConfigured() throws SQLException {
        stubFaultState(0, true);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> faultInjectionService.applyFaults("CREATE_TASK"));

        assertEquals("Simulated message corruption while processing: CREATE_TASK", exception.getMessage());
        verify(distributedEventService).record("MESSAGE_CORRUPTION_TRIGGERED", "operation=CREATE_TASK");
    }

    @Test
    void applyFaults_AppliesDelayBeforeThrowingOnCorruption_WhenBothConfigured() throws SQLException {
        stubFaultState(20, true);

        assertThrows(IllegalStateException.class, () -> faultInjectionService.applyFaults("DELETE_TASK"));

        verify(distributedEventService).record("NETWORK_DELAY_APPLIED", "operation=DELETE_TASK,delayMs=20");
        verify(distributedEventService).record("MESSAGE_CORRUPTION_TRIGGERED", "operation=DELETE_TASK");
    }
}
