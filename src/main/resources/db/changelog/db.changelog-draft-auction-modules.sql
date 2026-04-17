CREATE TABLE draft_room
(
    room_code  VARCHAR(255) PRIMARY KEY,
    state_json TEXT NOT NULL
);

CREATE TABLE auction_room
(
    room_code      VARCHAR(255) PRIMARY KEY,
    snapshot_json  TEXT NOT NULL
);
