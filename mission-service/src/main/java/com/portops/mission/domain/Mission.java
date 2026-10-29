package com.portops.mission.domain;

import com.portops.contract.MissionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Agrégat Mission. Les règles métier vivent ici (pas dans le service ni le contrôleur).
 * equals/hashCode basés sur l'identifiant uniquement : jamais de Lombok @Data sur une entité JPA.
 */
@Entity
@Table(name = "mission")
public class Mission {

    @Id
    private UUID id;

    @Column(name = "container_id", nullable = false, length = 11)
    private String containerId;

    @Column(name = "equipment_id")
    private String equipmentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MissionStatus status;

    /**
     * Verrou optimiste : deux superviseurs qui changent le statut en même temps → un seul gagne (409).
     * Type objet (Long) volontaire : null = entité nouvelle → Spring Data fait persist() et non merge()
     * (avec un id UUID assigné et un long primitif, save() ferait un SELECT inutile avant chaque INSERT).
     */
    @Version
    private Long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Mission() {
        // requis par JPA
    }

    private Mission(UUID id, String containerId, String equipmentId, Instant now) {
        this.id = id;
        this.containerId = containerId;
        this.equipmentId = equipmentId;
        this.status = MissionStatus.CREATED;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static Mission create(String containerId, String equipmentId, Clock clock) {
        Objects.requireNonNull(containerId, "containerId");
        return new Mission(UUID.randomUUID(), containerId, equipmentId, clock.instant());
    }

    public void changeStatus(MissionStatus target, Clock clock) {
        if (!status.canTransitionTo(target)) {
            throw new IllegalStatusTransitionException(id, status, target);
        }
        this.status = target;
        this.updatedAt = clock.instant();
    }

    public void assignEquipment(String equipmentId, Clock clock) {
        this.equipmentId = equipmentId;
        this.updatedAt = clock.instant();
    }

    public UUID getId() {
        return id;
    }

    public String getContainerId() {
        return containerId;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public MissionStatus getStatus() {
        return status;
    }

    public Long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Mission other && id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
