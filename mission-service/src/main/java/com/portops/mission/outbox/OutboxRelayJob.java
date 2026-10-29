package com.portops.mission.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Déclencheur planifié, séparé du bean transactionnel.
 * PIÈGE évité : un @Scheduled qui appelle this.methodeTransactionnelle() contourne le proxy Spring
 * (auto-invocation) → aucune transaction, le FOR UPDATE SKIP LOCKED ne verrouille rien.
 */
@Component
@ConditionalOnProperty(name = "portops.outbox.relay-enabled", havingValue = "true", matchIfMissing = true)
public class OutboxRelayJob {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelayJob.class);

    private final OutboxPublisher publisher;

    public OutboxRelayJob(OutboxPublisher publisher) {
        this.publisher = publisher;
    }

    @Scheduled(fixedDelayString = "${portops.outbox.poll-interval-ms:500}")
    public void relay() {
        int published = publisher.publishPendingBatch();
        if (published > 0) {
            log.debug("{} événement(s) outbox publiés", published);
        }
    }
}
