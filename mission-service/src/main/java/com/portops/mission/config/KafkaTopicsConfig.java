package com.portops.mission.config;

import com.portops.contract.Topics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Le producteur possède son topic. En production, les topics sont plutôt créés par l'IaC
 * (Terraform / Strimzi KafkaTopic) : on garde ce bean pour le poste de dev.
 */
@Configuration
public class KafkaTopicsConfig {

    @Bean
    NewTopic missionEventsTopic(@Value("${portops.kafka.replication-factor:1}") short replicationFactor,
                                @Value("${portops.kafka.min-insync-replicas:1}") String minInsync) {
        return TopicBuilder.name(Topics.MISSION_EVENTS)
                .partitions(Topics.MISSION_EVENTS_PARTITIONS)
                .replicas(replicationFactor)
                // prod : replication-factor 3 + min.insync.replicas 2 + acks=all → aucune perte si un broker tombe
                .config("min.insync.replicas", minInsync)
                .config("retention.ms", String.valueOf(7L * 24 * 3600 * 1000))
                .build();
    }
}
