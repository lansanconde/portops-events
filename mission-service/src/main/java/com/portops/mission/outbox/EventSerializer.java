package com.portops.mission.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portops.contract.MissionEvent;
import org.springframework.stereotype.Component;

/**
 * Sérialisation centralisée avec l'ObjectMapper configuré par Spring Boot
 * (dates ISO-8601, modules Java Time). Jamais de {@code new ObjectMapper()} par message.
 */
@Component
public class EventSerializer {

    private final ObjectMapper objectMapper;

    public EventSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String toJson(MissionEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Sérialisation impossible de l'événement " + event.eventId(), e);
        }
    }
}
