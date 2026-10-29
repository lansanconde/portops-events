package com.portops.mission.api;

import com.portops.contract.MissionStatus;
import com.portops.mission.application.MissionService;
import com.portops.mission.domain.Mission;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/missions")
public class MissionController {

    private final MissionService service;

    public MissionController(MissionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<MissionResponse> create(@Valid @RequestBody CreateMissionRequest request) {
        Mission mission = service.create(request.containerId(), request.equipmentId());
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(mission.getId()).toUri();
        return ResponseEntity.created(location).body(MissionResponse.from(mission));
    }

    @PatchMapping("/{id}/status")
    public MissionResponse changeStatus(@PathVariable UUID id, @Valid @RequestBody ChangeStatusRequest request) {
        return MissionResponse.from(service.changeStatus(id, request.status()));
    }

    @GetMapping("/{id}")
    public MissionResponse get(@PathVariable UUID id) {
        return MissionResponse.from(service.get(id));
    }

    /** Numéro de conteneur ISO 6346 : 4 lettres + 7 chiffres (ex. CMAU1234567). */
    public record CreateMissionRequest(
            @NotNull @Pattern(regexp = "[A-Z]{4}\\d{7}") String containerId,
            String equipmentId) {
    }

    public record ChangeStatusRequest(@NotNull MissionStatus status) {
    }

    public record MissionResponse(UUID id, String containerId, String equipmentId,
                                  MissionStatus status, Long version, Instant updatedAt) {
        static MissionResponse from(Mission m) {
            return new MissionResponse(m.getId(), m.getContainerId(), m.getEquipmentId(),
                    m.getStatus(), m.getVersion(), m.getUpdatedAt());
        }
    }
}
