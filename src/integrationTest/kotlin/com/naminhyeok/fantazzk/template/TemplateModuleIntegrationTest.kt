package com.naminhyeok.fantazzk.template

import com.naminhyeok.fantazzk.template.application.CreateTemplateCommand
import com.naminhyeok.fantazzk.template.application.TemplateCreateService
import com.naminhyeok.fantazzk.template.application.TemplateFinder
import com.naminhyeok.fantazzk.template.repository.TemplatePlayerRepository
import com.naminhyeok.fantazzk.template.repository.TemplateRepository
import com.naminhyeok.fantazzk.template.spi.TemplateLookup
import com.naminhyeok.fantazzk.template.spi.TemplateLookupException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.modulith.test.ApplicationModuleTest
import org.springframework.modulith.test.PublishedEvents
import org.springframework.modulith.test.Scenario

@ApplicationModuleTest(
    module = "template",
    verifyAutomatically = false,
)
class TemplateModuleIntegrationTest {
    @Autowired
    lateinit var templateCreateService: TemplateCreateService

    @Autowired
    lateinit var templateLookup: TemplateLookup

    @Autowired
    lateinit var templateFinder: TemplateFinder

    @Autowired
    lateinit var templateRepository: TemplateRepository

    @Autowired
    lateinit var templatePlayerRepository: TemplatePlayerRepository

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun `template module boots in standalone mode`() {
        assertThat(templateCreateService).isNotNull()
        assertThat(templateLookup).isNotNull()
    }

    @Test
    fun `template create publishes TemplateCreated`(publishedEvents: PublishedEvents) {
        templateCreateService.create(
            CreateTemplateCommand.Auction(
                name = "모듈 테스트 템플릿",
                teamCount = 2,
                teamSize = 2,
                budget = 300,
                playerNames = listOf("선수1", "선수2"),
            ),
        )

        val events =
            publishedEvents
                .ofType(TemplateCreated::class.java)
                .matching { it.name == "모듈 테스트 템플릿" }
                .toList()

        assertThat(events).hasSize(1)
    }

    @Test
    fun `template create 이후 finder 목록에서 새 템플릿을 조회할 수 있다`(scenario: Scenario) {
        scenario
            .stimulate {
                templateCreateService.create(
                    CreateTemplateCommand.Auction(
                        name = "프로젝션 템플릿",
                        teamCount = 2,
                        teamSize = 2,
                        budget = 300,
                        playerNames = listOf("선수1", "선수2"),
                    ),
                )
            }
            .andWaitForStateChange({ templateFinder.list() }) { templates ->
                templates.any { it.name == "프로젝션 템플릿" }
            }
            .andVerify { templates ->
                assertThat(templates.map { it.name }).contains("프로젝션 템플릿")
            }
    }

    @Test
    fun `template finder 목록 조회는 유효하지 않은 legacy row가 있어도 정상 템플릿을 반환한다`() {
        templateRepository.save(
            Template.create(
                name = "정상 템플릿",
                configuration = TemplateConfiguration.Auction(teamCount = 2, teamSize = 2, budgetValue = 300),
            ),
        )
        jdbcTemplate.update(
            """
            insert into template (name, mode, team_count, team_size, budget, draft_order_strategy)
            values (?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            "유효하지 않은 legacy 템플릿",
            "AUCTION",
            2,
            2,
            null,
            null,
        )

        val templates = templateFinder.list()

        assertThat(templates.map { it.name }).contains("정상 템플릿")
        assertThat(templates.map { it.name }).doesNotContain("유효하지 않은 legacy 템플릿")
    }

    @Test
    fun `template finder 상세 조회는 displayOrder 순서의 선수 목록을 반환한다`() {
        val template =
            templateRepository.save(
                Template.create(
                    name = "순서 검증",
                    configuration = TemplateConfiguration.Draft(teamCount = 2, teamSize = 3, strategy = DraftOrderStrategy.SNAKE),
                ),
            )
        templatePlayerRepository.saveAll(
            listOf(
                TemplatePlayer(templateId = template.templateId, name = "선수3", displayOrder = 2),
                TemplatePlayer(templateId = template.templateId, name = "선수1", displayOrder = 0),
                TemplatePlayer(templateId = template.templateId, name = "선수2", displayOrder = 1),
                TemplatePlayer(templateId = template.templateId, name = "선수4", displayOrder = 3),
            ),
        )

        val detail = templateFinder.getDetail(TemplateId(template.templateId))

        assertThat(detail.players.map { it.name }).containsExactly("선수1", "선수2", "선수3", "선수4")
    }

    @Test
    fun `template lookup 은 aggregate 기반 상세 조회를 snapshot 으로 변환한다`() {
        val template =
            templateRepository.save(
                Template.create(
                    name = "snapshot 검증",
                    configuration = TemplateConfiguration.Draft(teamCount = 2, teamSize = 3, strategy = DraftOrderStrategy.FIXED),
                ),
            )
        templatePlayerRepository.saveAll(
            listOf(
                TemplatePlayer(templateId = template.templateId, name = "선수4", displayOrder = 3),
                TemplatePlayer(templateId = template.templateId, name = "선수1", displayOrder = 0),
                TemplatePlayer(templateId = template.templateId, name = "선수3", displayOrder = 2),
                TemplatePlayer(templateId = template.templateId, name = "선수2", displayOrder = 1),
            ),
        )

        val snapshot = templateLookup.getTemplate(template.templateId)

        assertThat(snapshot.mode.name).isEqualTo("DRAFT")
        assertThat(snapshot.teamCount).isEqualTo(2)
        assertThat(snapshot.teamSize).isEqualTo(3)
        assertThat(snapshot.draftOrderStrategy?.name).isEqualTo("FIXED")
        assertThat(snapshot.players.map { it.name }).containsExactly("선수1", "선수2", "선수3", "선수4")
    }

    @Test
    fun `template lookup 은 존재하지 않는 템플릿을 not found 로 변환한다`() {
        assertThatThrownBy { templateLookup.getTemplate(999_999L) }
            .isInstanceOf(TemplateLookupException.NotFound::class.java)
    }

    @Test
    fun `template lookup 은 유효하지 않은 roster 를 invalid 로 변환한다`() {
        val templateId =
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
                300,
                null,
            )!!

        templatePlayerRepository.saveAll(
            listOf(
                TemplatePlayer(templateId = templateId, name = "선수1", displayOrder = 0),
            ),
        )

        assertThatThrownBy { templateLookup.getTemplate(templateId) }
            .isInstanceOf(TemplateLookupException.Invalid::class.java)
    }
}
