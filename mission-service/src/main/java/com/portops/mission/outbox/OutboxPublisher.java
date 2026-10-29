package com.portops.mission.outbox;

import com.portops.contract.Topics;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Relais outbox → Kafka.
 *
 * <ul>
 *   <li>clé = missionId → ordre garanti par mission (une mission = une partition) ;</li>
 *   <li>timestamp du record = occurredAt → Kafka Streams fenêtre sur le temps MÉTIER, pas sur l'heure de publication ;</li>
 *   <li>on attend l'ack du broker (get) avant de marquer publié : sinon on perd des messages ;</li>
 *   <li>au premier échec on s'arrête : publier la suite romprait l'ordre.</li>
 * </ul>
 */
@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Clock clock;
    private final int batchSize;
    private final Duration sendTimeout;

    public OutboxPublisher(OutboxRepository repository,
                           KafkaTemplate<String, String> kafkaTemplate,
                           Clock clock,
                           @Value("${portops.outbox.batch-size:100}") int batchSize,
                           @Value("${portops.outbox.send-timeout:5s}") Duration sendTimeout) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.clock = clock;
        this.batchSize = batchSize;
        this.sendTimeout = sendTimeout;
    }

    /**
     * La transaction couvre le verrou SKIP LOCKED et la mise à jour published_at.
     * Si le service crashe après l'ack Kafka mais avant le commit SQL, l'événement sera republié :
     * c'est le contrat at-least-once, assumé et compensé par l'idempotence des consommateurs.
     *
     * @return nombre d'événements publiés
     */
    @Transactional
    public int publishPendingBatch() {
        List<OutboxEvent> batch = repository.lockNextBatch(batchSize);
        int published = 0;
        for (OutboxEvent event : batch) {
            ProducerRecord<String, String> record = new ProducerRecord<>(
                    Topics.MISSION_EVENTS,
                    null,
                    event.getOccurredAt().toEpochMilli(),
                    event.getAggregateId(),
                    event.getPayload());
            try {
                kafkaTemplate.send(record).get(sendTimeout.toMillis(), TimeUnit.MILLISECONDS);
                event.markPublished(clock.instant());
                published++;
            } catch (ExecutionException | TimeoutException e) {
                event.recordFailure(e.getMessage());
                log.warn("Publication outbox échouée pour l'événement {} (tentative {}), arrêt du lot pour préserver l'ordre",
                        event.getId(), event.getAttempts(), e);
                break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Relais outbox interrompu");
                break;
            }
        }
        return published;
    }
}
