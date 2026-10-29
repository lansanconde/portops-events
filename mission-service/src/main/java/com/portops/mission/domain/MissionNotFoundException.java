package com.portops.mission.domain;

import java.util.UUID;

public class MissionNotFoundException extends RuntimeException {

    public MissionNotFoundException(UUID id) {
        super("Mission %s introuvable".formatted(id));
    }
}
