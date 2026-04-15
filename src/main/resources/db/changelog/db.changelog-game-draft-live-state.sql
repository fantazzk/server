CREATE TABLE game_draft_member
(
    members_game_id UUID         NOT NULL,
    member_order    INT          NOT NULL,
    team_leader_id  VARCHAR(255) NOT NULL,
    player_name     VARCHAR(255) NOT NULL,
    assign_order    INT          NOT NULL,
    CONSTRAINT pk_game_draft_member PRIMARY KEY (members_game_id, member_order),
    CONSTRAINT fk_game_draft_member_game
        FOREIGN KEY (members_game_id) REFERENCES games (game_id)
);

CREATE INDEX idx_game_draft_member_game_id ON game_draft_member (members_game_id);
