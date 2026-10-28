package com.portops.contract;

import java.util.EnumSet;
import java.util.Set;

/**
 * Cycle de vie d'une mission de manutention.
 * Les transitions autorisées sont portées par le contrat pour que producteur
 * et consommateurs partagent exactement la même règle métier.
 */
public enum MissionStatus {
    CREATED,
    ASSIGNED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    public Set<MissionStatus> allowedNext() {
        return switch (this) {
            case CREATED -> EnumSet.of(ASSIGNED, CANCELLED);
            case ASSIGNED -> EnumSet.of(IN_PROGRESS, CANCELLED);
            case IN_PROGRESS -> EnumSet.of(COMPLETED, CANCELLED);
            case COMPLETED, CANCELLED -> EnumSet.noneOf(MissionStatus.class);
        };
    }

    public boolean canTransitionTo(MissionStatus target) {
        return target != null && allowedNext().contains(target);
    }

    public boolean isTerminal() {
        return allowedNext().isEmpty();
    }
}
