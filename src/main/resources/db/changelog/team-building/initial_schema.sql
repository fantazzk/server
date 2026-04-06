CREATE TABLE template
(
    id                   UUID PRIMARY KEY,
    name                 VARCHAR(255) NOT NULL,
    mode                 VARCHAR(20)  NOT NULL,
    team_count           INT          NOT NULL,
    team_size            INT          NOT NULL,
    budget               INT,
    draft_order_strategy VARCHAR(20),
    created_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE template_player
(
    id            UUID PRIMARY KEY,
    template_id   UUID         NOT NULL,
    name          VARCHAR(255) NOT NULL,
    display_order INT          NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_template_player_template FOREIGN KEY (template_id) REFERENCES template (id)
);

CREATE TABLE room
(
    id                    UUID PRIMARY KEY,
    code                  VARCHAR(6)  NOT NULL UNIQUE,
    host_id               VARCHAR(36) NOT NULL,
    status                VARCHAR(20) NOT NULL,
    mode                  VARCHAR(20) NOT NULL,
    team_count            INT         NOT NULL,
    team_size             INT         NOT NULL,
    budget                INT,
    draft_order_strategy  VARCHAR(20),
    current_turn_index    INT,
    current_auction_round INT,
    created_at            TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_room_code ON room (code);

CREATE TABLE room_player
(
    id            UUID PRIMARY KEY,
    room_id       UUID         NOT NULL,
    name          VARCHAR(255) NOT NULL,
    status        VARCHAR(20)  NOT NULL,
    display_order INT          NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_room_player_room FOREIGN KEY (room_id) REFERENCES room (id)
);

CREATE TABLE room_team_leader
(
    id               UUID PRIMARY KEY,
    room_id          UUID         NOT NULL,
    team_leader_id   VARCHAR(36)  NOT NULL,
    nickname         VARCHAR(255) NOT NULL,
    remaining_budget INT,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_room_team_leader_room FOREIGN KEY (room_id) REFERENCES room (id)
);

CREATE TABLE room_team_member
(
    id             UUID PRIMARY KEY,
    room_id        UUID         NOT NULL,
    team_leader_id VARCHAR(36)  NOT NULL,
    player_name    VARCHAR(255) NOT NULL,
    assign_order   INT          NOT NULL,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_room_team_member_room FOREIGN KEY (room_id) REFERENCES room (id)
);

CREATE TABLE room_bid
(
    id             UUID PRIMARY KEY,
    room_id        UUID        NOT NULL,
    round          INT         NOT NULL,
    team_leader_id VARCHAR(36) NOT NULL,
    amount         INT         NOT NULL,
    created_at     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_room_bid_room FOREIGN KEY (room_id) REFERENCES room (id)
);
