CREATE TABLE IF NOT EXISTS room_view
(
  room_id BIGINT PRIMARY KEY,
  code    VARCHAR(6)  NOT NULL UNIQUE,
  status  VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS room_team_leader_view
(
  id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  room_id          BIGINT       NOT NULL,
  team_leader_id   VARCHAR(36)  NOT NULL,
  nickname         VARCHAR(255) NOT NULL,
  remaining_budget INT,
  CONSTRAINT uq_room_team_leader_view UNIQUE (room_id, team_leader_id)
);
