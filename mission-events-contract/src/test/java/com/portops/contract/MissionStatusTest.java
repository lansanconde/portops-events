package com.portops.contract;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class MissionStatusTest {

    @ParameterizedTest(name = "{0} -> {1} autorisé = {2}")
    @CsvSource({
            "CREATED, ASSIGNED, true",
            "CREATED, IN_PROGRESS, false",
            "ASSIGNED, IN_PROGRESS, true",
            "IN_PROGRESS, COMPLETED, true",
            "IN_PROGRESS, CREATED, false",
            "COMPLETED, CANCELLED, false",
            "CANCELLED, ASSIGNED, false"
    })
    void transitions(MissionStatus from, MissionStatus to, boolean allowed) {
        assertThat(from.canTransitionTo(to)).isEqualTo(allowed);
    }

    @Test
    void terminalStates() {
        assertThat(MissionStatus.COMPLETED.isTerminal()).isTrue();
        assertThat(MissionStatus.CANCELLED.isTerminal()).isTrue();
        assertThat(MissionStatus.IN_PROGRESS.isTerminal()).isFalse();
    }

    @Test
    void nullTargetIsRejected() {
        assertThat(MissionStatus.CREATED.canTransitionTo(null)).isFalse();
    }
}
