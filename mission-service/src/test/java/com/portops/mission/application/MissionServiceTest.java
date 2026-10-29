package com.portops.mission.application;

import com.portops.contract.MissionEvent;
import com.portops.contract.MissionEventType;
import com.portops.contract.MissionStatus;
import com.portops.mission.domain.IllegalStatusTransitionException;
import com.portops.mission.domain.Mission;
import com.portops.mission.domain.MissionRepository;
import com.portops.mission.outbox.OutboxWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MissionServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-10-30T08:00:00Z"), ZoneOffset.UTC);

    @Mock
    MissionRepository missions;
    @Mock
    OutboxWriter outbox;

    MissionService service;

    @BeforeEach
    void setUp() {
        service = new MissionService(missions, outbox, clock);
    }

    @Test
    void createWritesMissionAndEventKeyedByMissionId() {
        when(missions.save(any(Mission.class))).thenAnswer(inv -> inv.getArgument(0));

        Mission mission = service.create("CMAU1234567", "RTG-07");

        ArgumentCaptor<MissionEvent> captor = ArgumentCaptor.forClass(MissionEvent.class);
        verify(outbox).append(captor.capture());
        MissionEvent event = captor.getValue();
        assertThat(event.type()).isEqualTo(MissionEventType.MISSION_CREATED);
        assertThat(event.partitionKey()).isEqualTo(mission.getId().toString());
        assertThat(event.schemaVersion()).isEqualTo(MissionEvent.CURRENT_SCHEMA_VERSION);
        assertThat(event.occurredAt()).isEqualTo(clock.instant());
    }

    @Test
    void illegalTransitionWritesNoEvent() {
        Mission mission = Mission.create("CMAU1234567", "RTG-07", clock);
        when(missions.findById(mission.getId())).thenReturn(Optional.of(mission));

        assertThatThrownBy(() -> service.changeStatus(mission.getId(), MissionStatus.COMPLETED))
                .isInstanceOf(IllegalStatusTransitionException.class);
        verify(outbox, never()).append(any());
    }
}
