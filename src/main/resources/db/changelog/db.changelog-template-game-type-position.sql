ALTER TABLE template ADD COLUMN game_type VARCHAR(255) NOT NULL;
ALTER TABLE template ADD COLUMN pick_ban_time INT NOT NULL;
ALTER TABLE template ADD COLUMN min_bid_unit INT;
ALTER TABLE template ADD COLUMN position_limit INT;

ALTER TABLE template_player ADD COLUMN position VARCHAR(255) NOT NULL;
