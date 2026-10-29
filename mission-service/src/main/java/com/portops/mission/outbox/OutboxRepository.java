package com.portops.mission.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {

    /**
     * Verrouille un lot d'événements non publiés.
     * SKIP LOCKED : plusieurs instances du service peuvent tourner sans publier deux fois la même ligne.
     * PIÈGE : avec plusieurs instances, deux événements d'une même mission peuvent partir en parallèle
     * dans le désordre. Voir ADR-0001 (verrou leader unique ou CDC Debezium si l'ordre strict est requis).
     */
    @Query(value = """
            SELECT * FROM outbox_event
            WHERE published_at IS NULL
            ORDER BY occurred_at
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxEvent> lockNextBatch(@Param("limit") int limit);
}
