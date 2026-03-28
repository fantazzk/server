CREATE TABLE template
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    mode         VARCHAR(20)  NOT NULL,
    rules_json   JSON         NOT NULL,
    players_json JSON         NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE room
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    code              VARCHAR(6)  NOT NULL UNIQUE,
    host_id           VARCHAR(36) NOT NULL,
    status            VARCHAR(20) NOT NULL,
    settings_json     JSON        NOT NULL,
    player_pool_json  JSON        NOT NULL,
    team_leaders_json JSON        NOT NULL,
    progression_json  JSON        NULL,
    result_json       JSON        NULL,
    created_at        TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_room_code ON room (code);
