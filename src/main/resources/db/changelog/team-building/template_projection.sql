CREATE TABLE IF NOT EXISTS template_view
(
  template_id           BIGINT PRIMARY KEY,
  name                  VARCHAR(255) NOT NULL,
  mode                  VARCHAR(20)  NOT NULL,
  team_count            INT          NOT NULL,
  team_size             INT          NOT NULL,
  budget                INT,
  draft_order_strategy  VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS template_player_view
(
  id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  template_id   BIGINT       NOT NULL,
  name          VARCHAR(255) NOT NULL,
  display_order INT          NOT NULL
);
