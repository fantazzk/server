package com.naminhyeok.fantazzk.template.repository

import com.naminhyeok.fantazzk.template.DraftOrderStrategy
import com.naminhyeok.fantazzk.template.TeamBuildingMode
import com.naminhyeok.fantazzk.template.TemplateId
import com.naminhyeok.fantazzk.template.domain.Template
import com.naminhyeok.fantazzk.template.domain.TemplateConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration
import org.springframework.dao.InvalidDataAccessApiUsageException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.TestConstructor

@ImportAutoConfiguration(
    LiquibaseAutoConfiguration::class,
)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class TemplateRepositoryIntegrationTest(
    private val cut: TemplateRepository,
    private val jdbcTemplate: JdbcTemplate,
) {
    @Test
    fun `템플릿을 저장하고 조회할 수 있다`() {
        val saved =
            cut.save(
                Template.createAuction(
                    name = "테스트 경매",
                    teamCount = 5,
                    teamSize = 5,
                    budget = 300,
                    playerNames = listOf(
                        "선수1", "선수2", "선수3", "선수4", "선수5", "선수6", "선수7", "선수8",
                        "선수9", "선수10", "선수11", "선수12", "선수13", "선수14", "선수15", "선수16",
                        "선수17", "선수18", "선수19", "선수20",
                    ),
                ),
            )

        assertThat(saved.templateId).isGreaterThan(0)

        val found = cut.findById(TemplateId(saved.templateId))
        assertThat(found).isNotNull
        assertThat(found!!.mode).isEqualTo(TeamBuildingMode.AUCTION)
        assertThat(found.budget).isEqualTo(300)
    }

    @Test
    fun `템플릿 aggregate 하나를 저장하면 선수 컬렉션까지 함께 조회된다`() {
        val saved =
            cut.save(
                Template.createAuction(
                    name = "통합 템플릿",
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                    playerNames = listOf("선수2", "선수1"),
                ),
            )

        val found = cut.findById(saved.getId())

        assertThat(found).isNotNull
        assertThat(found!!.players().map { it.name }).containsExactly("선수1", "선수2")
    }

    @Test
    fun `드래프트 템플릿도 강타입 설정으로 복원된다`() {
        val saved =
            cut.save(
                Template.createDraft(
                    name = "드래프트",
                    teamCount = 2,
                    teamSize = 2,
                    strategy = DraftOrderStrategy.SNAKE,
                    playerNames = listOf("선수1", "선수2"),
                ),
            )

        val found = cut.findById(TemplateId(saved.templateId))

        assertThat(found).isNotNull
        assertThat(found!!.configuration)
            .isEqualTo(TemplateConfiguration.draft(teamCount = 2, teamSize = 2, strategy = DraftOrderStrategy.SNAKE))
    }

    @Test
    fun `전체 템플릿 목록을 조회할 수 있다`() {
        cut.save(
            Template.createAuction(
                name = "첫째",
                teamCount = 2,
                teamSize = 2,
                budget = 300,
                playerNames = listOf("선수1", "선수2"),
            ),
        )
        cut.save(
            Template.createDraft(
                name = "둘째",
                teamCount = 2,
                teamSize = 2,
                strategy = DraftOrderStrategy.SNAKE,
                playerNames = listOf("선수1", "선수2"),
            ),
        )

        val all = cut.findAll()
        assertThat(all).hasSizeGreaterThanOrEqualTo(2)
        assertThat(all.map { it.name }).contains("첫째", "둘째")
    }

    @Test
    fun `전체 템플릿 목록 조회는 유효하지 않은 row를 만나면 즉시 실패한다`() {
        cut.save(
            Template.createAuction(
                name = "정상 템플릿",
                teamCount = 2,
                teamSize = 2,
                budget = 300,
                playerNames = listOf("선수1", "선수2"),
            ),
        )
        val invalidTemplateId =
            jdbcTemplate.queryForObject(
                """
                insert into template (name, mode, team_count, team_size, budget, draft_order_strategy)
                values (?, ?, ?, ?, ?, ?)
                returning id
                """.trimIndent(),
                Long::class.java,
                "유효하지 않은 템플릿",
                "AUCTION",
                2,
                2,
                null,
                null,
            )!!

        try {
            assertThatThrownBy { cut.findAll() }
                .isInstanceOf(InvalidDataAccessApiUsageException::class.java)
                .hasMessage("경매 템플릿에는 예산이 필요합니다")
        } finally {
            jdbcTemplate.update("delete from template where id = ?", invalidTemplateId)
        }
    }

    @Test
    fun `aggregate 에 저장된 선수는 displayOrder 순서로 재수화된다`() {
        val saved =
            cut.save(
                Template.createAuction(
                    name = "드래프트",
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                    playerNames = listOf("선수2", "선수1"),
                ),
            )

        val found = cut.findById(saved.getId())

        assertThat(found).isNotNull
        assertThat(found!!.players().map { it.name }).containsExactly("선수1", "선수2")
    }

    @Test
    fun `유효하지 않은 row는 조회 시 즉시 실패한다`() {
        val templateId =
            jdbcTemplate.queryForObject(
                """
                insert into template (name, mode, team_count, team_size, budget, draft_order_strategy)
                values (?, ?, ?, ?, ?, ?)
                returning id
                """.trimIndent(),
                Long::class.java,
                "레거시 경매 템플릿",
                "AUCTION",
                2,
                2,
                null,
                null,
            )!!

        try {
            assertThatThrownBy { cut.findById(TemplateId(templateId)) }
                .isInstanceOf(InvalidDataAccessApiUsageException::class.java)
                .hasMessage("경매 템플릿에는 예산이 필요합니다")
        } finally {
            jdbcTemplate.update("delete from template where id = ?", templateId)
        }
    }
}
