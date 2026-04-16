ALTER TABLE room_team_member ADD COLUMN player_id INT;

UPDATE room_team_member
SET player_id = (
    SELECT room_player.room_player_id
    FROM room_player
    WHERE room_player.players_room_id = room_team_member.members_room_id
      AND room_player.name = room_team_member.player_name
);

ALTER TABLE room_team_member ALTER COLUMN player_id SET NOT NULL;
ALTER TABLE room_team_member DROP COLUMN player_name;
