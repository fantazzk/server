package com.naminhyeok.fantazzk.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
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
        "spring.datasource.url=jdbc:h2:mem:template-player-schema-migration-test;DB_CLOSE_DELAY=-1",
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
class TemplatePlayerSchemaMigrationIntegrationTest {
    private static final String TEMPLATE_ID = "11111111-1111-1111-1111-111111111111";

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final Templates templates;

    @BeforeEach
    void setUpLegacySchemaAndRows() {
        new ResourceDatabasePopulator(
            new ClassPathResource("db/changelog/db.changelog-initial.sql"),
            new ByteArrayResource(
                """
                INSERT INTO template (
                    template_id,
                    name,
                    mode,
                    team_count,
                    team_size,
                    budget,
                    draft_order_strategy
                ) VALUES (
                    '%s',
                    '레거시 경매전',
                    'AUCTION',
                    2,
                    2,
                    300,
                    NULL
                );

                INSERT INTO template_player (template_player_id, players_template_id, name, display_order) VALUES
                    ('22222222-2222-2222-2222-222222222222', '%s', '선수2', 1),
                    ('33333333-3333-3333-3333-333333333333', '%s', '선수1', 0);
                """.formatted(TEMPLATE_ID, TEMPLATE_ID, TEMPLATE_ID).getBytes(StandardCharsets.UTF_8)
            ),
            new ClassPathResource("db/changelog/db.changelog-template-player-aggregate-local-identity.sql")
        ).execute(dataSource);
    }

    @Test
    @Transactional(readOnly = true)
    void 레거시_template_player_rows를_마이그레이션한_뒤_현재_매핑으로_읽을_수_있다() {
        assertThat(countColumn("template_player", "template_player_id")).isZero();

        Template migrated = templates.findById(new TemplateId(UUID.fromString(TEMPLATE_ID))).orElseThrow();

        assertThat(migrated.getName()).isEqualTo("레거시 경매전");
        assertThat(migrated.getPlayers())
            .extracting("playerIndex", "name")
            .containsExactly(
                tuple(0, "선수1"),
                tuple(1, "선수2")
            );
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
