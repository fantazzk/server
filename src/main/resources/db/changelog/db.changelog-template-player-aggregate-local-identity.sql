CREATE TABLE template_player_v2
(
    players_template_id UUID         NOT NULL,
    name                VARCHAR(255) NOT NULL,
    display_order       INT          NOT NULL,
    PRIMARY KEY (players_template_id, display_order),
    FOREIGN KEY (players_template_id) REFERENCES template (template_id)
);

INSERT INTO template_player_v2 (players_template_id, name, display_order)
SELECT players_template_id, name, display_order
FROM template_player;

DROP TABLE template_player;

ALTER TABLE template_player_v2 RENAME TO template_player;

CREATE INDEX idx_template_player_template_id ON template_player (players_template_id);
