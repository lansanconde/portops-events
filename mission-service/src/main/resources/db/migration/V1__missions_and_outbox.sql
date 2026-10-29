CREATE TABLE mission (
    id            UUID PRIMARY KEY,
    container_id  VARCHAR(11)  NOT NULL,
    equipment_id  VARCHAR(64),
    status        VARCHAR(20)  NOT NULL,
    version       BIGINT       NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_mission_status ON mission (status);

CREATE TABLE outbox_event (
    id            UUID PRIMARY KEY,          -- = eventId, clé d'idempotence côté consommateurs
    aggregate_id  VARCHAR(64)  NOT NULL,     -- = clé Kafka (missionId)
    event_type    VARCHAR(64)  NOT NULL,
    payload       TEXT         NOT NULL,
    occurred_at   TIMESTAMPTZ  NOT NULL,
    published_at  TIMESTAMPTZ,
    attempts      INT          NOT NULL DEFAULT 0,
    last_error    VARCHAR(500)
);

-- index partiel : le relais ne lit que les lignes non publiées, l'index reste petit
CREATE INDEX idx_outbox_unpublished ON outbox_event (occurred_at) WHERE published_at IS NULL;
