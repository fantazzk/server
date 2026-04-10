package com.naminhyeok.fantazzk.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.nio.charset.StandardCharsets;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
    properties = {
        "spring.datasource.url=jdbc:h2:mem:room-schema-migration-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.liquibase.enabled=false",
        "sentry.enabled=false"
    }
)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class RoomSchemaMigrationIntegrationTest {
    private static final String ROOM_ID = "11111111-1111-1111-1111-111111111111";
    private static final String HOST_LEADER_ROW_ID = "22222222-2222-2222-2222-222222222222";
    private static final String GUEST_LEADER_ROW_ID = "33333333-3333-3333-3333-333333333333";
    private static final String MEMBER_ROW_ID = "44444444-4444-4444-4444-444444444444";
    private static final String FIRST_BID_ROW_ID = "55555555-5555-5555-5555-555555555555";

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final Rooms rooms;
    private final PlaceBid placeBid;

    @BeforeEach
    void setUpLegacyRoomSchemaAndRows() {
        new ResourceDatabasePopulator(
            new ClassPathResource("db/changelog/db.changelog-initial.sql"),
            new ClassPathResource("db/changelog/db.changelog-room-team-leader-action-token.sql"),
            new ClassPathResource("db/changelog/db.changelog-room-team-leader-draft-position.sql"),
            new ClassPathResource("db/changelog/db.changelog-room-created-at.sql"),
            new ClassPathResource("db/changelog/db.changelog-room-created-at-index.sql"),
            new ByteArrayResource(
                """
                INSERT INTO rooms (
                    room_id,
                    code,
                    created_at,
                    host_id,
                    status,
                    mode,
                    team_count,
                    team_size,
                    budget,
                    draft_order_strategy,
                    current_turn_index,
                    current_auction_round
                ) VALUES (
                    '%s',
                    'MIG001',
                    TIMESTAMP WITH TIME ZONE '2026-04-09T00:00:00Z',
                    'host-1',
                    'IN_PROGRESS',
                    'AUCTION',
                    2,
                    3,
                    300,
                    NULL,
                    NULL,
                    1
                );

                INSERT INTO room_player (room_player_id, players_room_id, name, display_order, status) VALUES
                    ('66666666-6666-6666-6666-666666666666', '%s', '중복선수', 0, 'ASSIGNED'),
                    ('77777777-7777-7777-7777-777777777777', '%s', '중복선수', 1, 'AVAILABLE'),
                    ('88888888-8888-8888-8888-888888888888', '%s', '새선수', 2, 'AVAILABLE');

                INSERT INTO room_team_leader (
                    room_team_leader_id,
                    leaders_room_id,
                    team_leader_id,
                    nickname,
                    remaining_budget,
                    action_token,
                    draft_position
                ) VALUES
                    ('%s', '%s', 'host-1', '호스트', 200, 'host-token', NULL),
                    ('%s', '%s', 'guest-1', '게스트', 300, 'guest-token', NULL);

                INSERT INTO room_team_member (room_team_member_id, members_room_id, team_leader_id, player_name, assign_order) VALUES
                    ('%s', '%s', 'host-1', '중복선수', 0);

                INSERT INTO room_bid (room_bid_id, bids_room_id, round, team_leader_id, amount) VALUES
                    ('%s', '%s', 1, 'host-1', 100);
                """.formatted(
                    ROOM_ID,
                    ROOM_ID,
                    ROOM_ID,
                    ROOM_ID,
                    HOST_LEADER_ROW_ID,
                    ROOM_ID,
                    GUEST_LEADER_ROW_ID,
                    ROOM_ID,
                    MEMBER_ROW_ID,
                    ROOM_ID,
                    FIRST_BID_ROW_ID,
                    ROOM_ID
                ).getBytes(StandardCharsets.UTF_8)
            ),
            new ClassPathResource("db/changelog/db.changelog-room-aggregate-local-identity.sql")
        ).execute(dataSource);
    }

    @Test
    @Transactional
    void 레거시_room_rows를_마이그레이션한_뒤_현재_매핑으로_읽고_같은_라운드_입찰을_이어갈_수_있다() {
        assertThat(countColumn("room_team_member", "room_player_id")).isZero();
        assertThat(countColumn("room_bid", "bid_sequence")).isEqualTo(1);

        Room migrated = rooms.findByCode("MIG001").orElseThrow();

        assertThat(migrated.getHostLeaderId()).isEqualTo(new TeamLeaderId("host-1"));
        assertThat(migrated.getPlayers())
            .extracting(RoomPlayer::getId, RoomPlayer::getName, RoomPlayer::getDisplayOrder, RoomPlayer::getStatus)
            .containsExactly(
                tuple(new RoomPlayerId(0), "중복선수", 0, PlayerStatus.ASSIGNED),
                tuple(new RoomPlayerId(1), "중복선수", 1, PlayerStatus.AVAILABLE),
                tuple(new RoomPlayerId(2), "새선수", 2, PlayerStatus.AVAILABLE)
            );
        assertThat(migrated.getMembers()).singleElement()
            .extracting(RoomTeamMember::teamLeaderId, RoomTeamMember::getPlayerName, RoomTeamMember::getAssignOrder)
            .containsExactly(new TeamLeaderId("host-1"), "중복선수", 0);

        RoomBid nextBid = placeBid.place("MIG001", "guest-1", 150);

        assertThat(nextBid.getRound()).isEqualTo(1);
        assertThat(nextBid.sequence()).isEqualTo(new BidSequence(2));
    }

    private int countColumn(String tableName, String columnName) {
        return jdbcTemplate.queryForObject(
            "select count(*) from information_schema.columns where table_name = upper(?) and column_name = upper(?)",
            Integer.class,
            tableName,
            columnName
        );
    }
}
