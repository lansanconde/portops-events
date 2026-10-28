package com.portops.contract;

/** Noms de topics centralisés : jamais de String en dur dans le code applicatif. */
public final class Topics {

    /** Événements de mission, clé = missionId. Version dans le nom : un changement cassant = un nouveau topic. */
    public static final String MISSION_EVENTS = "portops.mission-events.v1";

    /**
     * Dead Letter Topic. Suffixe ".DLT" = convention par défaut de DeadLetterPublishingRecoverer,
     * qui republie sur la MÊME partition : le DLT doit donc avoir au moins autant de partitions que la source.
     */
    public static final String MISSION_EVENTS_DLT = MISSION_EVENTS + ".DLT";

    /** Missions terminées par équipement et par fenêtre d'une heure (sortie Kafka Streams). */
    public static final String EQUIPMENT_THROUGHPUT = "portops.equipment-throughput.v1";

    /** Nombre de partitions du topic principal. Le changer casse l'affectation clé → partition. */
    public static final int MISSION_EVENTS_PARTITIONS = 6;

    private Topics() {
    }
}
