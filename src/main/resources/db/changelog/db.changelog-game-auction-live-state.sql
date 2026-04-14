CREATE TABLE game_auction_bid
(
    bids_game_id    UUID         NOT NULL,
    bid_order       INT          NOT NULL,
    round           INT          NOT NULL,
    bid_sequence    INT          NOT NULL,
    team_leader_id  VARCHAR(255) NOT NULL,
    amount          INT          NOT NULL,
    CONSTRAINT pk_game_auction_bid PRIMARY KEY (bids_game_id, bid_order),
    CONSTRAINT fk_game_auction_bid_game
        FOREIGN KEY (bids_game_id) REFERENCES games (game_id)
);

CREATE TABLE game_auction_member
(
    members_game_id UUID         NOT NULL,
    member_order    INT          NOT NULL,
    team_leader_id  VARCHAR(255) NOT NULL,
    player_name     VARCHAR(255) NOT NULL,
    assign_order    INT          NOT NULL,
    CONSTRAINT pk_game_auction_member PRIMARY KEY (members_game_id, member_order),
    CONSTRAINT fk_game_auction_member_game
        FOREIGN KEY (members_game_id) REFERENCES games (game_id)
);

CREATE INDEX idx_game_auction_bid_game_id ON game_auction_bid (bids_game_id);
CREATE INDEX idx_game_auction_member_game_id ON game_auction_member (members_game_id);
