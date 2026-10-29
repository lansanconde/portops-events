package com.portops.mission.outbox;

import com.portops.contract.MissionEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Écrit un événement dans l'outbox. Doit être appelé DANS la transaction métier (MANDATORY). */
@Component
public class OutboxWriter {

    private final OutboxRepository repository;
    private final EventSerializer serializer;

    public OutboxWriter(OutboxRepository repository, EventSerializer serializer) {
        this.repository = repository;
        this.serializer = serializer;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void append(MissionEvent event) {
        repository.save(new OutboxEvent(
                event.eventId(),
                event.partitionKey(),
                event.type().name(),
                serializer.toJson(event),
                event.occurredAt()));
    }
}
