CREATE TABLE template
(
    template_id           UUID PRIMARY KEY,
    name                  VARCHAR(255) NOT NULL,
    mode                  VARCHAR(255) NOT NULL,
    team_count            INT          NOT NULL,
    team_size             INT          NOT NULL,
    budget                INT,
    draft_order_strategy  VARCHAR(255)
);

CREATE TABLE template_player
(
    players_template_id UUID         NOT NULL,
    name                VARCHAR(255) NOT NULL,
    display_order       INT          NOT NULL,
    CONSTRAINT pk_template_player PRIMARY KEY (players_template_id, display_order),
    CONSTRAINT fk_template_player_template
        FOREIGN KEY (players_template_id) REFERENCES template (template_id)
);

CREATE TABLE rooms
(
    room_id                UUID PRIMARY KEY,
    code                   VARCHAR(255)             NOT NULL,
    created_at             TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    host_id                VARCHAR(255)             NOT NULL,
    status                 VARCHAR(255)             NOT NULL,
    mode                   VARCHAR(255)             NOT NULL,
    team_count             INT                      NOT NULL,
    team_size              INT                      NOT NULL,
    budget                 INT,
    draft_order_strategy   VARCHAR(255),
    current_turn_index     INT,
    current_auction_round  INT,
    CONSTRAINT uk_rooms_code UNIQUE (code)
);

CREATE TABLE room_player
(
    players_room_id UUID         NOT NULL,
    room_player_id  INT          NOT NULL,
    name            VARCHAR(255) NOT NULL,
    display_order   INT          NOT NULL,
    status          VARCHAR(255) NOT NULL,
    CONSTRAINT pk_room_player PRIMARY KEY (players_room_id, room_player_id),
    CONSTRAINT fk_room_player_room
        FOREIGN KEY (players_room_id) REFERENCES rooms (room_id)
);

CREATE TABLE room_team_leader
(
    leaders_room_id  UUID         NOT NULL,
    team_leader_id   VARCHAR(255) NOT NULL,
    nickname         VARCHAR(255) NOT NULL,
    remaining_budget INT,
    action_token     VARCHAR(255),
    draft_position   INT,
    CONSTRAINT pk_room_team_leader PRIMARY KEY (leaders_room_id, team_leader_id),
    CONSTRAINT fk_room_team_leader_room
        FOREIGN KEY (leaders_room_id) REFERENCES rooms (room_id)
);

CREATE TABLE room_team_member
(
    members_room_id UUID         NOT NULL,
    assign_order    INT          NOT NULL,
    team_leader_id  VARCHAR(255) NOT NULL,
    player_name     VARCHAR(255) NOT NULL,
    CONSTRAINT pk_room_team_member PRIMARY KEY (members_room_id, assign_order),
    CONSTRAINT fk_room_team_member_room
        FOREIGN KEY (members_room_id) REFERENCES rooms (room_id)
);

CREATE TABLE room_bid
(
    bids_room_id   UUID         NOT NULL,
    round          INT          NOT NULL,
    bid_sequence   INT          NOT NULL,
    team_leader_id VARCHAR(255) NOT NULL,
    amount         INT          NOT NULL,
    CONSTRAINT pk_room_bid PRIMARY KEY (bids_room_id, round, bid_sequence),
    CONSTRAINT fk_room_bid_room
        FOREIGN KEY (bids_room_id) REFERENCES rooms (room_id)
);

CREATE TABLE event_publication
(
    id                     UUID                     NOT NULL,
    listener_id            VARCHAR(255)             NOT NULL,
    event_type             VARCHAR(255)             NOT NULL,
    serialized_event       TEXT                     NOT NULL,
    publication_date       TIMESTAMP WITH TIME ZONE NOT NULL,
    completion_date        TIMESTAMP WITH TIME ZONE,
    status                 VARCHAR(255),
    completion_attempts    INT,
    last_resubmission_date TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (id)
);

CREATE INDEX idx_template_player_template_id ON template_player (players_template_id);
CREATE INDEX idx_rooms_status_created_at ON rooms (status, created_at);
CREATE INDEX idx_room_player_room_id ON room_player (players_room_id);
CREATE INDEX idx_room_team_leader_room_id ON room_team_leader (leaders_room_id);
CREATE INDEX idx_room_team_member_room_id ON room_team_member (members_room_id);
CREATE INDEX idx_room_bid_room_id ON room_bid (bids_room_id);
CREATE INDEX idx_event_publication_by_completion_date ON event_publication (completion_date);
