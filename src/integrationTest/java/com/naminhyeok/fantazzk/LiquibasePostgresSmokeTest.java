package com.naminhyeok.fantazzk;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;

@ActiveProfiles("test")
@SpringBootTest(
    properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "sentry.enabled=false"
    }
)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class LiquibasePostgresSmokeTest {
    private final JdbcTemplate jdbcTemplate;

    LiquibasePostgresSmokeTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Test
    void liquibase_실제_postgresql_스키마에서_rooms_변경을_검증한다() {
        assertThat(countTable("rooms")).isEqualTo(1);
        assertThat(countColumn("rooms", "created_at")).isEqualTo(1);
        assertThat(countColumn("rooms", "min_bid_unit")).isEqualTo(1);
        assertThat(isNullable("rooms", "min_bid_unit")).isTrue();
        assertThat(countColumn("rooms", "position_limit")).isEqualTo(1);
        assertThat(isNullable("rooms", "position_limit")).isTrue();
        assertThat(countColumn("rooms", "started_game_id")).isEqualTo(1);
        assertThat(countColumn("rooms", "started_at")).isEqualTo(1);
        assertThat(countColumn("rooms", "current_turn_index")).isZero();
        assertThat(countColumn("rooms", "current_auction_round")).isZero();
        assertThat(countColumn("rooms", "current_auction_round_ends_at")).isZero();
        assertThat(countIndex("rooms", "idx_rooms_status_created_at")).isEqualTo(1);
        assertThat(indexColumns("rooms", "idx_rooms_status_created_at")).containsExactly("status", "created_at");
        assertThat(countTable("room_team_member")).isZero();
        assertThat(countTable("room_bid")).isZero();
        assertThat(countTable("game_draft_member")).isEqualTo(1);
        assertThat(countColumn("game_draft_member", "members_game_id")).isEqualTo(1);
        assertThat(countColumn("game_draft_member", "member_order")).isEqualTo(1);
        assertThat(countColumn("game_draft_member", "team_leader_id")).isEqualTo(1);
        assertThat(countColumn("game_draft_member", "player_name")).isEqualTo(1);
        assertThat(countColumn("game_draft_member", "assign_order")).isEqualTo(1);
        assertThat(countIndex("game_draft_member", "idx_game_draft_member_game_id")).isEqualTo(1);
        assertThat(indexColumns("game_draft_member", "idx_game_draft_member_game_id")).containsExactly("members_game_id");
    }

    private Integer countTable(String tableName) {
        return jdbcTemplate.queryForObject(
            "select count(*) from information_schema.tables where table_schema = current_schema() and table_name = ?",
            Integer.class,
            tableName
        );
    }

    private Integer countColumn(String tableName, String columnName) {
        return jdbcTemplate.queryForObject(
            "select count(*) from information_schema.columns where table_schema = current_schema() and table_name = ? and column_name = ?",
            Integer.class,
            tableName,
            columnName
        );
    }

    private Integer countIndex(String tableName, String indexName) {
        return jdbcTemplate.queryForObject(
            "select count(*) from pg_indexes where schemaname = current_schema() and tablename = ? and indexname = ?",
            Integer.class,
            tableName,
            indexName
        );
    }

    private boolean isNullable(String tableName, String columnName) {
        String nullable =
            jdbcTemplate.queryForObject(
                "select is_nullable from information_schema.columns where table_schema = current_schema() and table_name = ? and column_name = ?",
                String.class,
                tableName,
                columnName
            );
        return "YES".equalsIgnoreCase(nullable);
    }

    private List<String> indexColumns(String tableName, String indexName) {
        return jdbcTemplate.execute((Connection connection) -> {
            var columnsByOrdinal = new TreeMap<Integer, String>();
            try (ResultSet resultSet = connection.getMetaData().getIndexInfo(null, currentSchema(connection), tableName, false, false)) {
                while (resultSet.next()) {
                    var currentIndexName = resultSet.getString("INDEX_NAME");
                    var columnName = resultSet.getString("COLUMN_NAME");
                    if (indexName.equalsIgnoreCase(currentIndexName) && columnName != null) {
                        columnsByOrdinal.put(resultSet.getInt("ORDINAL_POSITION"), columnName);
                    }
                }
            }
            return new ArrayList<>(columnsByOrdinal.values());
        });
    }

    private String currentSchema(Connection connection) {
        try (ResultSet resultSet = connection.createStatement().executeQuery("select current_schema()")) {
            resultSet.next();
            return resultSet.getString(1);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
