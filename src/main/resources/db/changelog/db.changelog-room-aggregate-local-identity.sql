CREATE TABLE room_player_v2
(
    players_room_id UUID         NOT NULL,
    room_player_id  INT          NOT NULL,
    name            VARCHAR(255) NOT NULL,
    display_order   INT          NOT NULL,
    status          VARCHAR(255) NOT NULL,
    PRIMARY KEY (players_room_id, room_player_id),
    CONSTRAINT fk_room_player_v2_room
        FOREIGN KEY (players_room_id) REFERENCES rooms (room_id)
);

INSERT INTO room_player_v2 (players_room_id, room_player_id, name, display_order, status)
SELECT players_room_id, display_order, name, display_order, status
FROM room_player;

DROP TABLE room_player;

ALTER TABLE room_player_v2 RENAME TO room_player;

CREATE INDEX idx_room_player_room_id ON room_player (players_room_id);

CREATE TABLE room_team_leader_v2
(
    leaders_room_id  UUID         NOT NULL,
    team_leader_id   VARCHAR(255) NOT NULL,
    nickname         VARCHAR(255) NOT NULL,
    remaining_budget INT,
    action_token     VARCHAR(255),
    draft_position   INT,
    PRIMARY KEY (leaders_room_id, team_leader_id),
    CONSTRAINT fk_room_team_leader_v2_room
        FOREIGN KEY (leaders_room_id) REFERENCES rooms (room_id)
);

INSERT INTO room_team_leader_v2 (leaders_room_id, team_leader_id, nickname, remaining_budget, action_token, draft_position)
SELECT leaders_room_id, team_leader_id, nickname, remaining_budget, action_token, draft_position
FROM room_team_leader;

DROP TABLE room_team_leader;

ALTER TABLE room_team_leader_v2 RENAME TO room_team_leader;

CREATE INDEX idx_room_team_leader_room_id ON room_team_leader (leaders_room_id);

CREATE TABLE room_team_member_v2
(
    members_room_id UUID         NOT NULL,
    assign_order    INT          NOT NULL,
    team_leader_id  VARCHAR(255) NOT NULL,
    room_player_id  INT,
    player_name     VARCHAR(255) NOT NULL,
    PRIMARY KEY (members_room_id, assign_order),
    CONSTRAINT fk_room_team_member_v2_room
        FOREIGN KEY (members_room_id) REFERENCES rooms (room_id)
);

INSERT INTO room_team_member_v2 (members_room_id, assign_order, team_leader_id, room_player_id, player_name)
SELECT member.members_room_id,
       member.assign_order,
       member.team_leader_id,
       player.room_player_id,
       member.player_name
FROM room_team_member member
LEFT JOIN room_player player
    ON player.players_room_id = member.members_room_id
   AND player.name = member.player_name;

DROP TABLE room_team_member;

ALTER TABLE room_team_member_v2 RENAME TO room_team_member;

CREATE INDEX idx_room_team_member_room_id ON room_team_member (members_room_id);

CREATE TABLE room_bid_v2
(
    bids_room_id   UUID         NOT NULL,
    round          INT          NOT NULL,
    bid_sequence   INT          NOT NULL,
    team_leader_id VARCHAR(255) NOT NULL,
    amount         INT          NOT NULL,
    PRIMARY KEY (bids_room_id, round, bid_sequence),
    CONSTRAINT fk_room_bid_v2_room
        FOREIGN KEY (bids_room_id) REFERENCES rooms (room_id)
);

INSERT INTO room_bid_v2 (bids_room_id, round, bid_sequence, team_leader_id, amount)
SELECT bids_room_id,
       round,
       ROW_NUMBER() OVER (PARTITION BY bids_room_id, round ORDER BY room_bid_id),
       team_leader_id,
       amount
FROM room_bid;

DROP TABLE room_bid;

ALTER TABLE room_bid_v2 RENAME TO room_bid;

CREATE INDEX idx_room_bid_room_id ON room_bid (bids_room_id);
