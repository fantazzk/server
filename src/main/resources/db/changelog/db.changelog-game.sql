CREATE TABLE games
(
    game_id               UUID PRIMARY KEY,
    room_id               UUID                     NOT NULL,
    room_code             VARCHAR(255)             NOT NULL,
    game_mode             VARCHAR(255)             NOT NULL,
    started_at            TIMESTAMP WITH TIME ZONE NOT NULL,
    status                VARCHAR(255)             NOT NULL,
    team_count            INT                      NOT NULL,
    team_size             INT                      NOT NULL,
    budget                INT,
    pick_ban_time         INT                      NOT NULL,
    min_bid_unit          INT,
    position_limit        INT,
    draft_order_strategy  VARCHAR(255),
    current_round         INT,
    current_round_ends_at TIMESTAMP WITH TIME ZONE,
    current_turn_index    INT,
    CONSTRAINT fk_games_room
        FOREIGN KEY (room_id) REFERENCES rooms (room_id),
    CONSTRAINT uk_games_room_id UNIQUE (room_id)
);

CREATE INDEX idx_games_room_id ON games (room_id);

CREATE TABLE game_participant
(
    participants_game_id UUID         NOT NULL,
    participant_order    INT          NOT NULL,
    team_leader_id       VARCHAR(255) NOT NULL,
    nickname             VARCHAR(255) NOT NULL,
    draft_position       INT,
    remaining_budget     INT,
    CONSTRAINT pk_game_participant PRIMARY KEY (participants_game_id, participant_order),
    CONSTRAINT fk_game_participant_game
        FOREIGN KEY (participants_game_id) REFERENCES games (game_id)
);

CREATE TABLE game_player
(
    player_pool_game_id UUID         NOT NULL,
    player_pool_order   INT          NOT NULL,
    player_id           INT          NOT NULL,
    name                VARCHAR(255) NOT NULL,
    position            VARCHAR(255) NOT NULL,
    display_order       INT          NOT NULL,
    CONSTRAINT pk_game_player PRIMARY KEY (player_pool_game_id, player_pool_order),
    CONSTRAINT fk_game_player_game
        FOREIGN KEY (player_pool_game_id) REFERENCES games (game_id)
);

CREATE INDEX idx_game_participant_game_id ON game_participant (participants_game_id);
CREATE INDEX idx_game_player_game_id ON game_player (player_pool_game_id);
