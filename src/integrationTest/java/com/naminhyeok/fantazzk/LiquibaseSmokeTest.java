package com.naminhyeok.fantazzk;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestConstructor;

@SpringBootTest(
    properties = {
        "spring.datasource.url=jdbc:h2:mem:liquibase-smoke;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=validate",
        "sentry.enabled=false"
    }
)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class LiquibaseSmokeTest {
    private final JdbcTemplate jdbcTemplate;

    LiquibaseSmokeTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Test
    void liquibase_초기_스키마로_자바_jpa_구조를_검증하며_부팅한다() {
        assertThat(countTable("template")).isEqualTo(1);
        assertThat(countTable("template_player")).isEqualTo(1);
        assertThat(countTable("rooms")).isEqualTo(1);
        assertThat(countColumn("rooms", "created_at")).isEqualTo(1);
        assertThat(countTable("room_player")).isEqualTo(1);
        assertThat(countTable("room_team_leader")).isEqualTo(1);
        assertThat(countColumn("room_team_leader", "action_token")).isEqualTo(1);
        assertThat(countColumn("room_team_leader", "draft_position")).isEqualTo(1);
        assertThat(countTable("room_team_member")).isEqualTo(1);
        assertThat(countTable("room_bid")).isEqualTo(1);
        assertThat(countTable("event_publication")).isEqualTo(1);
    }

    private Integer countTable(String tableName) {
        return jdbcTemplate.queryForObject(
            "select count(*) from information_schema.tables where table_name = upper(?)",
            Integer.class,
            tableName
        );
    }

    private Integer countColumn(String tableName, String columnName) {
        return jdbcTemplate.queryForObject(
            "select count(*) from information_schema.columns where table_name = upper(?) and column_name = upper(?)",
            Integer.class,
            tableName,
            columnName
        );
    }
}
