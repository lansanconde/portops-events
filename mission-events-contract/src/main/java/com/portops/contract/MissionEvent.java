package com.portops.contract;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Événement publié sur {@link Topics#MISSION_EVENTS}.
 *
 * <p>Règles de compatibilité (à faire respecter en revue de code) :
 * <ul>
 *   <li>on peut AJOUTER un champ optionnel sans changer de version ;</li>
 *   <li>renommer, supprimer ou changer le type d'un champ = changement cassant
 *       → nouvelle version de schéma ET nouveau topic (…-events.v2), migration des consommateurs d'abord ;</li>
 *   <li>{@code eventId} est la clé d'idempotence côté consommateurs ;</li>
 *   <li>la clé Kafka est toujours {@code missionId} : c'est elle qui garantit l'ordre par mission.</li>
 * </ul>
 */
public record MissionEvent(
        UUID eventId,
        int schemaVersion,
        MissionEventType type,
        UUID missionId,
        String containerId,
        String equipmentId,
        MissionStatus status,
        Instant occurredAt) {

    public static final int CURRENT_SCHEMA_VERSION = 1;

    public MissionEvent {
        Objects.requireNonNull(eventId, "eventId");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(missionId, "missionId");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(occurredAt, "occurredAt");
    }

    public static MissionEvent of(MissionEventType type, UUID missionId, String containerId,
                                  String equipmentId, MissionStatus status, Instant occurredAt) {
        return new MissionEvent(UUID.randomUUID(), CURRENT_SCHEMA_VERSION, type, missionId,
                containerId, equipmentId, status, occurredAt);
    }

    /** Clé de partitionnement Kafka : toutes les versions d'une mission vont dans la même partition. */
    public String partitionKey() {
        return missionId.toString();
    }
}
