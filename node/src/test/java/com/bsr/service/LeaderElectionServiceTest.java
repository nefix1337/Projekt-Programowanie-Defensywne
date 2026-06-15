package com.bsr.service;

import com.bsr.config.TaskRabbitMqConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Testy serwisu LeaderElectionService")
@ExtendWith(MockitoExtension.class)
class LeaderElectionServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private RabbitListenerEndpointRegistry listenerRegistry;

    @Mock
    private MessageListenerContainer listenerContainer;

    private LeaderElectionService service;

    @BeforeEach
    void setUp() {
        service = new LeaderElectionService(jdbcTemplate, listenerRegistry, "node-1", 100, 15);
        lenient().when(listenerRegistry.getListenerContainer(anyString())).thenReturn(listenerContainer);
    }

    private void stubForcedDown(boolean forcedDown) {
        when(jdbcTemplate.query(contains("SELECT forced_down"), ArgumentMatchers.<ResultSetExtractor<Object>>any(), any()))
                .thenReturn(forcedDown);
    }

    private void stubCurrentLeader(Optional<String> leaderId) {
        when(jdbcTemplate.query(contains("ORDER BY node_weight"), ArgumentMatchers.<ResultSetExtractor<Object>>any(), any()))
                .thenReturn(leaderId);
    }

    private AtomicBoolean leaderFlag() {
        return (AtomicBoolean) ReflectionTestUtils.getField(service, "leader");
    }

    @Test
    @DisplayName("Inicjalizacja tworzy tabelę kandydatów i dodaje kolumny do wstrzykiwania awarii")
    void initialize_CreatesTableAndAddsFaultInjectionColumns() {
        service.initialize();

        verify(jdbcTemplate, times(4)).execute(anyString());
    }

    @Test
    @DisplayName("Węzeł staje się liderem, gdy ma najwyższą wagę i jest aktywny")
    void updateLeadership_BecomesLeader_WhenHighestWeightAndAlive() {
        stubForcedDown(false);
        stubCurrentLeader(Optional.of("node-1"));
        when(listenerContainer.isRunning()).thenReturn(false);

        service.updateLeadership();

        ArgumentCaptor<Object[]> heartbeatArgs = ArgumentCaptor.forClass(Object[].class);
        verify(jdbcTemplate).update(contains("INSERT INTO node_leader_candidates"), heartbeatArgs.capture());
        assertArrayEquals(new Object[] {"node-1", 100, heartbeatArgs.getValue()[2]}, heartbeatArgs.getValue());

        verify(listenerContainer, times(TaskRabbitMqConfig.WRITE_LISTENER_IDS.size())).start();
        assertTrue(leaderFlag().get());
    }

    @Test
    @DisplayName("Węzeł ustępuje z roli lidera, gdy innym węzłem staje się lider")
    void updateLeadership_StepsDown_WhenAnotherNodeBecomesLeader() {
        leaderFlag().set(true);
        stubForcedDown(false);
        stubCurrentLeader(Optional.of("node-2"));
        when(listenerContainer.isRunning()).thenReturn(true);

        service.updateLeadership();

        verify(listenerContainer, times(TaskRabbitMqConfig.WRITE_LISTENER_IDS.size())).stop();
        assertFalse(leaderFlag().get());
    }

    @Test
    @DisplayName("Zatrzymanie nasłuchiwania i przerwanie działania, gdy węzeł jest wymuszony do wyłączenia")
    void updateLeadership_StopsListenersAndReturnsEarly_WhenForcedDown() {
        leaderFlag().set(true);
        stubForcedDown(true);
        when(listenerContainer.isRunning()).thenReturn(true);

        service.updateLeadership();

        verify(listenerContainer, times(TaskRabbitMqConfig.WRITE_LISTENER_IDS.size())).stop();
        assertFalse(leaderFlag().get());
        verify(jdbcTemplate, never()).update(anyString(), any(Object[].class));
    }

    @Test
    @DisplayName("Węzeł pozostaje obserwatorem, gdy inny węzeł ma wyższą wagę")
    void updateLeadership_RemainsFollower_WhenAnotherNodeHasHigherWeight() {
        stubForcedDown(false);
        stubCurrentLeader(Optional.of("node-2"));

        service.updateLeadership();

        verify(listenerContainer, never()).start();
        verify(listenerContainer, never()).stop();
        assertFalse(leaderFlag().get());
    }

    @Test
    @DisplayName("Wyrejestrowanie zatrzymuje nasłuchiwanie i usuwa wpis kandydata")
    void unregister_StopsListenersAndRemovesCandidateRow() {
        when(listenerContainer.isRunning()).thenReturn(true);

        service.unregister();

        verify(listenerContainer, times(TaskRabbitMqConfig.WRITE_LISTENER_IDS.size())).stop();

        ArgumentCaptor<Object[]> argsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(jdbcTemplate).update(eq("DELETE FROM node_leader_candidates WHERE node_id = ?"), argsCaptor.capture());
        assertArrayEquals(new Object[] {"node-1"}, argsCaptor.getValue());
    }
}
