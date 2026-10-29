package com.portops.mission.domain;

import com.portops.contract.MissionStatus;

import java.util.UUID;

public class IllegalStatusTransitionException extends RuntimeException {

    public IllegalStatusTransitionException(UUID missionId, MissionStatus from, MissionStatus to) {
        super("Mission %s : transition %s -> %s interdite".formatted(missionId, from, to));
    }
}
