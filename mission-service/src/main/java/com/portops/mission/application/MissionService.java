package com.portops.mission.application;

import com.portops.contract.MissionEvent;
import com.portops.contract.MissionEventType;
import com.portops.contract.MissionStatus;
import com.portops.mission.domain.Mission;
import com.portops.mission.domain.MissionNotFoundException;
import com.portops.mission.domain.MissionRepository;
import com.portops.mission.outbox.OutboxWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

/**
 * Cas d'usage. Règle d'or : AUCUN appel Kafka ici.
 * L'état (mission) et l'intention de publier (outbox) sont commités ensemble ou pas du tout :
 * c'est ce qui élimine le « dual write ».
 */
@Service
public class MissionService {

    private final MissionRepository missions;
    private final OutboxWriter outbox;
    private final Clock clock;

    public MissionService(MissionRepository missions, OutboxWriter outbox, Clock clock) {
        this.missions = missions;
        this.outbox = outbox;
        this.clock = clock;
    }

    @Transactional
    public Mission create(String containerId, String equipmentId) {
        Mission mission = missions.save(Mission.create(containerId, equipmentId, clock));
        outbox.append(toEvent(MissionEventType.MISSION_CREATED, mission));
        return mission;
    }

    @Transactional
    public Mission changeStatus(UUID missionId, MissionStatus target) {
        Mission mission = missions.findById(missionId)
                .orElseThrow(() -> new MissionNotFoundException(missionId));
        mission.changeStatus(target, clock);
        outbox.append(toEvent(MissionEventType.MISSION_STATUS_CHANGED, mission));
        return mission;
    }

    @Transactional(readOnly = true)
    public Mission get(UUID missionId) {
        return missions.findById(missionId).orElseThrow(() -> new MissionNotFoundException(missionId));
    }

    private MissionEvent toEvent(MissionEventType type, Mission mission) {
        return MissionEvent.of(type, mission.getId(), mission.getContainerId(),
                mission.getEquipmentId(), mission.getStatus(), mission.getUpdatedAt());
    }
}
