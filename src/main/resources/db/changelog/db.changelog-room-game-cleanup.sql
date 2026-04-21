UPDATE rooms
SET started_game_id = COALESCE(
        started_game_id,
        (SELECT g.game_id FROM games g WHERE g.room_id = rooms.room_id),
        room_id
    ),
    started_at = COALESCE(
        started_at,
        (SELECT g.started_at FROM games g WHERE g.room_id = rooms.room_id),
        created_at
    )
WHERE status IN ('IN_PROGRESS', 'COMPLETED');

INSERT INTO games (
    game_id,
    room_id,
    room_code,
    game_mode,
    started_at,
    status,
    team_count,
    team_size,
    budget,
    pick_ban_time,
    min_bid_unit,
    position_limit,
    draft_order_strategy,
    current_round,
    current_round_ends_at,
    current_turn_index,
    version
)
SELECT
    r.started_game_id,
    r.room_id,
    r.code,
    r.mode,
    r.started_at,
    CASE
        WHEN r.status = 'COMPLETED' THEN 'COMPLETED'
        ELSE 'IN_PROGRESS'
    END,
    r.team_count,
    r.team_size,
    r.budget,
    r.pick_ban_time,
    r.min_bid_unit,
    r.position_limit,
    r.draft_order_strategy,
    CASE
        WHEN r.mode = 'AUCTION' THEN COALESCE(r.current_auction_round, 1)
        ELSE NULL
    END,
    CASE
        WHEN r.mode = 'AUCTION' AND r.status = 'IN_PROGRESS' THEN r.current_auction_round_ends_at
        ELSE NULL
    END,
    CASE
        WHEN r.mode = 'DRAFT' THEN COALESCE(r.current_turn_index, 0)
        ELSE NULL
    END,
    0
FROM rooms r
WHERE r.status IN ('IN_PROGRESS', 'COMPLETED')
    AND NOT EXISTS (SELECT 1 FROM games g WHERE g.room_id = r.room_id);

INSERT INTO game_participant (
    participants_game_id,
    participant_order,
    team_leader_id,
    nickname,
    draft_position,
    remaining_budget
)
SELECT
    leader_rows.started_game_id,
    leader_rows.participant_order,
    leader_rows.team_leader_id,
    leader_rows.nickname,
    leader_rows.draft_position,
    leader_rows.remaining_budget
FROM (
    SELECT
        r.started_game_id,
        rtl.team_leader_id,
        rtl.nickname,
        rtl.draft_position,
        rtl.remaining_budget,
        ROW_NUMBER() OVER (
            PARTITION BY rtl.leaders_room_id
            ORDER BY
                CASE WHEN rtl.team_leader_id = r.host_id THEN 0 ELSE 1 END,
                COALESCE(rtl.draft_position, 2147483647),
                rtl.team_leader_id
        ) - 1 AS participant_order
    FROM rooms r
    JOIN room_team_leader rtl ON rtl.leaders_room_id = r.room_id
    WHERE r.status IN ('IN_PROGRESS', 'COMPLETED')
) leader_rows
WHERE NOT EXISTS (
    SELECT 1
    FROM game_participant gp
    WHERE gp.participants_game_id = leader_rows.started_game_id
);

INSERT INTO game_player (
    player_pool_game_id,
    player_pool_order,
    player_id,
    name,
    position,
    display_order
)
SELECT
    player_rows.started_game_id,
    player_rows.player_pool_order,
    player_rows.player_id,
    player_rows.name,
    COALESCE(player_rows.position, ''),
    player_rows.display_order
FROM (
    SELECT
        r.started_game_id,
        rp.room_player_id AS player_id,
        rp.name,
        rp.position,
        rp.display_order,
        ROW_NUMBER() OVER (
            PARTITION BY rp.players_room_id
            ORDER BY rp.display_order, rp.room_player_id
        ) - 1 AS player_pool_order
    FROM rooms r
    JOIN room_player rp ON rp.players_room_id = r.room_id
    WHERE r.status IN ('IN_PROGRESS', 'COMPLETED')
) player_rows
WHERE NOT EXISTS (
    SELECT 1
    FROM game_player gp
    WHERE gp.player_pool_game_id = player_rows.started_game_id
);

INSERT INTO game_auction_member (
    members_game_id,
    member_order,
    team_leader_id,
    player_name,
    assign_order
)
SELECT
    member_rows.started_game_id,
    member_rows.member_order,
    member_rows.team_leader_id,
    member_rows.player_name,
    member_rows.assign_order
FROM (
    SELECT
        r.started_game_id,
        rtm.team_leader_id,
        rtm.player_name,
        rtm.assign_order,
        ROW_NUMBER() OVER (
            PARTITION BY rtm.members_room_id
            ORDER BY rtm.assign_order
        ) - 1 AS member_order
    FROM rooms r
    JOIN room_team_member rtm ON rtm.members_room_id = r.room_id
    WHERE r.status IN ('IN_PROGRESS', 'COMPLETED')
        AND r.mode = 'AUCTION'
) member_rows
WHERE NOT EXISTS (
    SELECT 1
    FROM game_auction_member gam
    WHERE gam.members_game_id = member_rows.started_game_id
);

INSERT INTO game_draft_member (
    members_game_id,
    member_order,
    team_leader_id,
    player_name,
    assign_order
)
SELECT
    member_rows.started_game_id,
    member_rows.member_order,
    member_rows.team_leader_id,
    member_rows.player_name,
    member_rows.assign_order
FROM (
    SELECT
        r.started_game_id,
        rtm.team_leader_id,
        rtm.player_name,
        rtm.assign_order,
        ROW_NUMBER() OVER (
            PARTITION BY rtm.members_room_id
            ORDER BY rtm.assign_order
        ) - 1 AS member_order
    FROM rooms r
    JOIN room_team_member rtm ON rtm.members_room_id = r.room_id
    WHERE r.status IN ('IN_PROGRESS', 'COMPLETED')
        AND r.mode = 'DRAFT'
) member_rows
WHERE NOT EXISTS (
    SELECT 1
    FROM game_draft_member gdm
    WHERE gdm.members_game_id = member_rows.started_game_id
);

INSERT INTO game_auction_bid (
    bids_game_id,
    bid_order,
    round,
    bid_sequence,
    team_leader_id,
    amount
)
SELECT
    bid_rows.started_game_id,
    bid_rows.bid_order,
    bid_rows.round,
    bid_rows.bid_sequence,
    bid_rows.team_leader_id,
    bid_rows.amount
FROM (
    SELECT
        r.started_game_id,
        rb.round,
        rb.bid_sequence,
        rb.team_leader_id,
        rb.amount,
        ROW_NUMBER() OVER (
            PARTITION BY rb.bids_room_id
            ORDER BY rb.round, rb.bid_sequence
        ) - 1 AS bid_order
    FROM rooms r
    JOIN room_bid rb ON rb.bids_room_id = r.room_id
    WHERE r.status IN ('IN_PROGRESS', 'COMPLETED')
        AND r.mode = 'AUCTION'
) bid_rows
WHERE NOT EXISTS (
    SELECT 1
    FROM game_auction_bid gab
    WHERE gab.bids_game_id = bid_rows.started_game_id
);

UPDATE rooms
SET status = 'STARTED'
WHERE status IN ('IN_PROGRESS', 'COMPLETED');

DROP TABLE IF EXISTS room_team_member;
DROP TABLE IF EXISTS room_bid;

ALTER TABLE rooms DROP COLUMN IF EXISTS current_turn_index;
ALTER TABLE rooms DROP COLUMN IF EXISTS current_auction_round;
ALTER TABLE rooms DROP COLUMN IF EXISTS current_auction_round_ends_at;
