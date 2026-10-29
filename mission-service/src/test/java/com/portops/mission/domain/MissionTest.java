package com.portops.mission.domain;

import com.portops.contract.MissionStatus;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MissionTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-10-30T08:00:00Z"), ZoneOffset.UTC);

    @Test
    void newMissionStartsCreated() {
        Mission mission = Mission.create("CMAU1234567", "RTG-07", clock);

        assertThat(mission.getStatus()).isEqualTo(MissionStatus.CREATED);
        assertThat(mission.getCreatedAt()).isEqualTo(clock.instant());
    }

    @Test
    void followsTheLifecycle() {
        Mission mission = Mission.create("CMAU1234567", "RTG-07", clock);

        mission.changeStatus(MissionStatus.ASSIGNED, clock);
        mission.changeStatus(MissionStatus.IN_PROGRESS, clock);
        mission.changeStatus(MissionStatus.COMPLETED, clock);

        assertThat(mission.getStatus()).isEqualTo(MissionStatus.COMPLETED);
    }

    @Test
    void rejectsSkippingSteps() {
        Mission mission = Mission.create("CMAU1234567", "RTG-07", clock);

        assertThatThrownBy(() -> mission.changeStatus(MissionStatus.COMPLETED, clock))
                .isInstanceOf(IllegalStatusTransitionException.class)
                .hasMessageContaining("CREATED -> COMPLETED");
    }
}
