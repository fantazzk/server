ALTER TABLE template ALTER COLUMN game_type DROP NOT NULL;
ALTER TABLE template DROP COLUMN position_limit;

ALTER TABLE template_player ALTER COLUMN position DROP NOT NULL;

ALTER TABLE rooms ADD COLUMN game_type VARCHAR(255);
ALTER TABLE rooms DROP COLUMN position_limit;

ALTER TABLE games ADD COLUMN game_type VARCHAR(255);
ALTER TABLE games DROP COLUMN position_limit;

ALTER TABLE game_player ALTER COLUMN position DROP NOT NULL;
