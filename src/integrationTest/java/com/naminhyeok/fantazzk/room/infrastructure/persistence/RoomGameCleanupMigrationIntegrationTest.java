package com.naminhyeok.fantazzk.room.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

class RoomGameCleanupMigrationIntegrationTest {
    @Test
    void 레거시_started_room_live_state를_game_테이블로_백필한_뒤_cleanup한다() {
        JdbcTemplate jdbcTemplate = jdbcTemplate();
        applyLegacySchema(jdbcTemplate);
        insertLegacyAuctionRoom(jdbcTemplate);
        insertLegacyDraftRoom(jdbcTemplate);

        new ResourceDatabasePopulator(new ClassPathResource("db/changelog/db.changelog-room-game-cleanup.sql"))
            .execute(jdbcTemplate.getDataSource());

        Map<String, Object> auctionRoom =
            jdbcTemplate.queryForMap("select status, started_game_id, started_at from rooms where room_id = ?", uuid("00000000-0000-0000-0000-0000000000a1"));
        assertThat(auctionRoom.get("status")).isEqualTo("STARTED");
        assertThat(auctionRoom.get("started_game_id")).isEqualTo(uuid("00000000-0000-0000-0000-0000000000a1"));
        assertThat(instantOf(auctionRoom.get("started_at"))).isEqualTo(Instant.parse("2026-04-10T00:00:00Z"));

        Map<String, Object> draftRoom =
            jdbcTemplate.queryForMap("select status, started_game_id, started_at from rooms where room_id = ?", uuid("00000000-0000-0000-0000-0000000000d1"));
        assertThat(draftRoom.get("status")).isEqualTo("STARTED");
        assertThat(draftRoom.get("started_game_id")).isEqualTo(uuid("00000000-0000-0000-0000-0000000000d9"));
        assertThat(instantOf(draftRoom.get("started_at"))).isEqualTo(Instant.parse("2026-04-11T00:00:00Z"));

        Map<String, Object> auctionGame =
            jdbcTemplate.queryForMap(
                "select game_id, status, current_round, current_round_ends_at from games where room_id = ?",
                uuid("00000000-0000-0000-0000-0000000000a1")
            );
        assertThat(auctionGame.get("game_id")).isEqualTo(uuid("00000000-0000-0000-0000-0000000000a1"));
        assertThat(auctionGame.get("status")).isEqualTo("IN_PROGRESS");
        assertThat(auctionGame.get("current_round")).isEqualTo(2);
        assertThat(instantOf(auctionGame.get("current_round_ends_at"))).isEqualTo(Instant.parse("2026-04-10T00:05:00Z"));

        Map<String, Object> draftGame =
            jdbcTemplate.queryForMap(
                "select game_id, status, current_turn_index from games where room_id = ?",
                uuid("00000000-0000-0000-0000-0000000000d1")
            );
        assertThat(draftGame.get("game_id")).isEqualTo(uuid("00000000-0000-0000-0000-0000000000d9"));
        assertThat(draftGame.get("status")).isEqualTo("COMPLETED");
        assertThat(draftGame.get("current_turn_index")).isEqualTo(2);

        assertThat(
            jdbcTemplate.queryForObject(
                "select count(*) from game_participant where participants_game_id in (?, ?)",
                Integer.class,
                uuid("00000000-0000-0000-0000-0000000000a1"),
                uuid("00000000-0000-0000-0000-0000000000d9")
            )
        ).isEqualTo(4);
        assertThat(
            jdbcTemplate.queryForObject(
                "select count(*) from game_player where player_pool_game_id in (?, ?)",
                Integer.class,
                uuid("00000000-0000-0000-0000-0000000000a1"),
                uuid("00000000-0000-0000-0000-0000000000d9")
            )
        ).isEqualTo(5);
        assertThat(jdbcTemplate.queryForObject("select count(*) from game_auction_member", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from game_auction_bid", Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("select count(*) from game_draft_member", Integer.class)).isEqualTo(2);
        assertThat(tableExists(jdbcTemplate, "ROOM_TEAM_MEMBER")).isFalse();
        assertThat(tableExists(jdbcTemplate, "ROOM_BID")).isFalse();
        assertThat(columnExists(jdbcTemplate, "ROOMS", "CURRENT_TURN_INDEX")).isFalse();
        assertThat(columnExists(jdbcTemplate, "ROOMS", "CURRENT_AUCTION_ROUND")).isFalse();
        assertThat(columnExists(jdbcTemplate, "ROOMS", "CURRENT_AUCTION_ROUND_ENDS_AT")).isFalse();
    }

    @Test
    void 레거시_null_position도_cleanup과_demotion을_거친다() {
        JdbcTemplate jdbcTemplate = jdbcTemplate();
        applyLegacySchema(jdbcTemplate);
        insertLegacyAuctionRoomWithNullPosition(jdbcTemplate);

        new ResourceDatabasePopulator(
            new ClassPathResource("db/changelog/db.changelog-room-game-cleanup.sql"),
            new ClassPathResource("db/changelog/db.changelog-metadata-demotion.sql")
        ).execute(jdbcTemplate.getDataSource());

        Map<String, Object> migratedPlayer =
            jdbcTemplate.queryForMap(
                "select position from game_player where player_pool_game_id = ? and player_id = ?",
                uuid("00000000-0000-0000-0000-0000000000b1"),
                0
            );

        assertThat(migratedPlayer.get("POSITION")).isEqualTo("");
    }

    private JdbcTemplate jdbcTemplate() {
        String databaseName = "room-game-cleanup-" + UUID.randomUUID();
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1".formatted(databaseName));
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return new JdbcTemplate(dataSource);
    }

    private void applyLegacySchema(JdbcTemplate jdbcTemplate) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
            new ClassPathResource("db/changelog/db.changelog-initial.sql"),
            new ClassPathResource("db/changelog/db.changelog-room-auction-deadline.sql"),
            new ClassPathResource("db/changelog/db.changelog-template-game-type-position.sql"),
            new ClassPathResource("db/changelog/db.changelog-room-pick-ban-time.sql"),
            new ClassPathResource("db/changelog/db.changelog-room-player-position.sql"),
            new ClassPathResource("db/changelog/db.changelog-room-min-bid-unit.sql"),
            new ClassPathResource("db/changelog/db.changelog-room-position-limit.sql"),
            new ClassPathResource("db/changelog/db.changelog-room-start-handoff.sql"),
            new ClassPathResource("db/changelog/db.changelog-game.sql"),
            new ClassPathResource("db/changelog/db.changelog-game-auction-live-state.sql"),
            new ClassPathResource("db/changelog/db.changelog-game-version.sql"),
            new ClassPathResource("db/changelog/db.changelog-game-draft-live-state.sql")
        );
        populator.execute(jdbcTemplate.getDataSource());
    }

    private void insertLegacyAuctionRoom(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update(
            """
            insert into rooms (
                room_id, code, created_at, host_id, status, mode, team_count, team_size, budget,
                draft_order_strategy, current_turn_index, current_auction_round, pick_ban_time,
                min_bid_unit, position_limit, current_auction_round_ends_at, started_game_id, started_at
            ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            uuid("00000000-0000-0000-0000-0000000000a1"),
            "AUC901",
            Instant.parse("2026-04-10T00:00:00Z").toString(),
            "host-a",
            "IN_PROGRESS",
            "AUCTION",
            2,
            3,
            300,
            null,
            null,
            2,
            45,
            10,
            null,
            Instant.parse("2026-04-10T00:05:00Z").toString(),
            null,
            null
        );

        jdbcTemplate.batchUpdate(
            "insert into room_player (players_room_id, room_player_id, name, display_order, status, position) values (?, ?, ?, ?, ?, ?)",
            List.of(
                new Object[] { uuid("00000000-0000-0000-0000-0000000000a1"), 0, "선수1", 0, "ASSIGNED", "TOP" },
                new Object[] { uuid("00000000-0000-0000-0000-0000000000a1"), 1, "선수2", 1, "AVAILABLE", "JUNGLE" },
                new Object[] { uuid("00000000-0000-0000-0000-0000000000a1"), 2, "선수3", 2, "AVAILABLE", "MID" }
            )
        );
        jdbcTemplate.batchUpdate(
            "insert into room_team_leader (leaders_room_id, team_leader_id, nickname, remaining_budget, action_token, draft_position) values (?, ?, ?, ?, ?, ?)",
            List.of(
                new Object[] { uuid("00000000-0000-0000-0000-0000000000a1"), "host-a", "호스트A", 200, "host-token-a", null },
                new Object[] { uuid("00000000-0000-0000-0000-0000000000a1"), "guest-a", "게스트A", 300, "guest-token-a", null }
            )
        );
        jdbcTemplate.update(
            "insert into room_team_member (members_room_id, assign_order, team_leader_id, player_name) values (?, ?, ?, ?)",
            uuid("00000000-0000-0000-0000-0000000000a1"),
            0,
            "host-a",
            "선수1"
        );
        jdbcTemplate.batchUpdate(
            "insert into room_bid (bids_room_id, round, bid_sequence, team_leader_id, amount) values (?, ?, ?, ?, ?)",
            List.of(
                new Object[] { uuid("00000000-0000-0000-0000-0000000000a1"), 2, 1, "host-a", 120 },
                new Object[] { uuid("00000000-0000-0000-0000-0000000000a1"), 2, 2, "guest-a", 130 }
            )
        );
    }

    private void insertLegacyDraftRoom(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update(
            """
            insert into rooms (
                room_id, code, created_at, host_id, status, mode, team_count, team_size, budget,
                draft_order_strategy, current_turn_index, current_auction_round, pick_ban_time,
                min_bid_unit, position_limit, current_auction_round_ends_at, started_game_id, started_at
            ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            uuid("00000000-0000-0000-0000-0000000000d1"),
            "DRF901",
            Instant.parse("2026-04-11T00:00:00Z").toString(),
            "host-d",
            "COMPLETED",
            "DRAFT",
            2,
            2,
            null,
            "SNAKE",
            2,
            null,
            30,
            null,
            null,
            null,
            uuid("00000000-0000-0000-0000-0000000000d9"),
            Instant.parse("2026-04-11T00:00:00Z").toString()
        );

        jdbcTemplate.batchUpdate(
            "insert into room_player (players_room_id, room_player_id, name, display_order, status, position) values (?, ?, ?, ?, ?, ?)",
            List.of(
                new Object[] { uuid("00000000-0000-0000-0000-0000000000d1"), 0, "선수A", 0, "ASSIGNED", "TOP" },
                new Object[] { uuid("00000000-0000-0000-0000-0000000000d1"), 1, "선수B", 1, "ASSIGNED", "JUNGLE" }
            )
        );
        jdbcTemplate.batchUpdate(
            "insert into room_team_leader (leaders_room_id, team_leader_id, nickname, remaining_budget, action_token, draft_position) values (?, ?, ?, ?, ?, ?)",
            List.of(
                new Object[] { uuid("00000000-0000-0000-0000-0000000000d1"), "host-d", "호스트D", null, "host-token-d", 1 },
                new Object[] { uuid("00000000-0000-0000-0000-0000000000d1"), "guest-d", "게스트D", null, "guest-token-d", 2 }
            )
        );
        jdbcTemplate.batchUpdate(
            "insert into room_team_member (members_room_id, assign_order, team_leader_id, player_name) values (?, ?, ?, ?)",
            List.of(
                new Object[] { uuid("00000000-0000-0000-0000-0000000000d1"), 0, "host-d", "선수A" },
                new Object[] { uuid("00000000-0000-0000-0000-0000000000d1"), 1, "guest-d", "선수B" }
            )
        );
    }

    private void insertLegacyAuctionRoomWithNullPosition(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update(
            """
            insert into rooms (
                room_id, code, created_at, host_id, status, mode, team_count, team_size, budget,
                draft_order_strategy, current_turn_index, current_auction_round, pick_ban_time,
                min_bid_unit, position_limit, current_auction_round_ends_at, started_game_id, started_at
            ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            uuid("00000000-0000-0000-0000-0000000000b1"),
            "AUC902",
            Instant.parse("2026-04-12T00:00:00Z").toString(),
            "host-b",
            "IN_PROGRESS",
            "AUCTION",
            2,
            2,
            300,
            null,
            null,
            1,
            45,
            10,
            null,
            Instant.parse("2026-04-12T00:05:00Z").toString(),
            null,
            null
        );

        jdbcTemplate.batchUpdate(
            "insert into room_player (players_room_id, room_player_id, name, display_order, status, position) values (?, ?, ?, ?, ?, ?)",
            List.of(
                new Object[] { uuid("00000000-0000-0000-0000-0000000000b1"), 0, "선수X", 0, "AVAILABLE", null },
                new Object[] { uuid("00000000-0000-0000-0000-0000000000b1"), 1, "선수Y", 1, "AVAILABLE", "JUNGLE" }
            )
        );
        jdbcTemplate.batchUpdate(
            "insert into room_team_leader (leaders_room_id, team_leader_id, nickname, remaining_budget, action_token, draft_position) values (?, ?, ?, ?, ?, ?)",
            List.of(
                new Object[] { uuid("00000000-0000-0000-0000-0000000000b1"), "host-b", "호스트B", 300, "host-token-b", null },
                new Object[] { uuid("00000000-0000-0000-0000-0000000000b1"), "guest-b", "게스트B", 300, "guest-token-b", null }
            )
        );
    }

    private UUID uuid(String value) {
        return UUID.fromString(value);
    }

    private boolean tableExists(JdbcTemplate jdbcTemplate, String tableName) {
        Integer count =
            jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where table_name = ?",
                Integer.class,
                tableName
            );
        return count != null && count > 0;
    }

    private boolean columnExists(JdbcTemplate jdbcTemplate, String tableName, String columnName) {
        Integer count =
            jdbcTemplate.queryForObject(
                "select count(*) from information_schema.columns where table_name = ? and column_name = ?",
                Integer.class,
                tableName,
                columnName
            );
        return count != null && count > 0;
    }

    private Instant instantOf(Object value) {
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.toInstant();
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toInstant();
        }
        throw new IllegalArgumentException("지원하지 않는 시간 타입입니다: " + value);
    }
}
