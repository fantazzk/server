package com.naminhyeok.fantazzk.template.repository.jdbc

import com.naminhyeok.fantazzk.template.infrastructure.TemplatePlayerRepository
import com.naminhyeok.fantazzk.template.infrastructure.TemplateRepository
import com.naminhyeok.fantazzk.template.model.DraftOrderStrategy
import com.naminhyeok.fantazzk.template.model.TeamBuildingMode
import com.naminhyeok.fantazzk.template.model.Template
import com.naminhyeok.fantazzk.template.model.TemplateConfiguration
import com.naminhyeok.fantazzk.template.model.TemplateIdentity
import com.naminhyeok.fantazzk.template.model.TemplatePlayer
import com.naminhyeok.fantazzk.template.model.configuration
import com.naminhyeok.fantazzk.template.model.of
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.TestConstructor

@ImportAutoConfiguration(
    LiquibaseAutoConfiguration::class,
    TemplateJdbcConfiguration::class,
    TemplateRepositoryAutoConfiguration::class,
)
@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class TemplateRepositoryIntegrationTest(
    private val cut: TemplateRepository,
    private val templatePlayerRepository: TemplatePlayerRepository,
    private val jdbcTemplate: JdbcTemplate,
) {
    @Test
    fun `템플릿을 저장하고 조회할 수 있다`() {
        val saved =
            cut.save(
                Template.create(
                    name = "테스트 경매",
                    configuration = TemplateConfiguration.Auction(teamCount = 5, teamSize = 5, budgetValue = 300),
                ),
            )

        assertThat(saved.templateId).isGreaterThan(0)

        val found = cut.findById(TemplateIdentity.of(saved.templateId))
        assertThat(found).isNotNull
        assertThat(found!!.mode).isEqualTo(TeamBuildingMode.AUCTION)
        assertThat(found.budget).isEqualTo(300)
    }

    @Test
    fun `드래프트 템플릿도 강타입 설정으로 복원된다`() {
        val saved =
            cut.save(
                Template.create(
                    name = "드래프트",
                    configuration = TemplateConfiguration.Draft(teamCount = 2, teamSize = 2, strategy = DraftOrderStrategy.SNAKE),
                ),
            )

        val found = cut.findById(TemplateIdentity.of(saved.templateId))

        assertThat(found).isNotNull
        assertThat(found!!.configuration)
            .isEqualTo(TemplateConfiguration.Draft(teamCount = 2, teamSize = 2, strategy = DraftOrderStrategy.SNAKE))
    }

    @Test
    fun `전체 템플릿 목록을 조회할 수 있다`() {
        cut.save(
            Template.create(
                name = "첫째",
                configuration = TemplateConfiguration.Auction(teamCount = 2, teamSize = 2, budgetValue = 300),
            ),
        )
        cut.save(
            Template.create(
                name = "둘째",
                configuration = TemplateConfiguration.Draft(teamCount = 2, teamSize = 2, strategy = DraftOrderStrategy.SNAKE),
            ),
        )

        val all = cut.findAll()
        assertThat(all).hasSizeGreaterThanOrEqualTo(2)
        assertThat(all.map { it.name }).contains("첫째", "둘째")
    }

    @Test
    fun `템플릿 선수를 displayOrder 순서로 조회할 수 있다`() {
        val template =
            cut.save(
                Template.create(
                    name = "드래프트",
                    configuration = TemplateConfiguration.Draft(teamCount = 2, teamSize = 2, strategy = DraftOrderStrategy.SNAKE),
                ),
            )

        templatePlayerRepository.saveAll(
            listOf(
                TemplatePlayer(templateId = template.templateId, name = "선수2", displayOrder = 1),
                TemplatePlayer(templateId = template.templateId, name = "선수1", displayOrder = 0),
            ),
        )

        val found = templatePlayerRepository.findByTemplateId(template.templateId)
        assertThat(found).hasSize(2)
        assertThat(found.map { it.name }).containsExactly("선수1", "선수2")
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

        assertThatThrownBy { cut.findById(TemplateIdentity.of(templateId)) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("경매 템플릿에는 예산이 필요합니다")
    }
}
