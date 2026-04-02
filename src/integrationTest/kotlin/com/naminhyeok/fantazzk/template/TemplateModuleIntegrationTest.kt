package com.naminhyeok.fantazzk.template

import com.naminhyeok.fantazzk.template.application.CreateTemplateCommand
import com.naminhyeok.fantazzk.template.application.TemplateCreateService
import com.naminhyeok.fantazzk.template.application.TemplateFinder
import com.naminhyeok.fantazzk.template.domain.Template
import com.naminhyeok.fantazzk.template.exception.TemplateException
import com.naminhyeok.fantazzk.template.repository.TemplateRepository
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
    lateinit var templateCatalog: TemplateCatalog

    @Autowired
    lateinit var templateFinder: TemplateFinder

    @Autowired
    lateinit var templateRepository: TemplateRepository

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun `템플릿 모듈은 루트 계약과 함께 단독 부팅된다`() {
        assertThat(templateCreateService).isNotNull()
        assertThat(templateCatalog).isNotNull()
    }

    @Test
    fun `템플릿 생성은 생성 이벤트를 발행한다`(publishedEvents: PublishedEvents) {
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
    fun `템플릿 생성 이후 조회 서비스 목록에서 새 템플릿을 조회할 수 있다`(scenario: Scenario) {
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
    fun `template finder 는 별도 선수 리포지토리 없이 aggregate 에서 선수 목록을 읽는다`() {
        val created =
            templateCreateService.create(
                CreateTemplateCommand.Auction(
                    name = "집약 루트",
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                    playerNames = listOf("선수1", "선수2"),
                ),
            )

        val detail = templateFinder.getDetail(created.getId())

        assertThat(detail.players.map { it.name }).containsExactly("선수1", "선수2")
    }

    @Test
    fun `템플릿 조회 서비스 목록 조회는 유효하지 않은 행을 템플릿 유효성 예외로 변환한다`() {
        templateRepository.save(
            Template(
                name = "정상 템플릿",
                templateConfiguration = TemplateConfiguration.Auction(teamCount = 2, teamSize = 2, budgetValue = 300),
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
            assertThatThrownBy { templateFinder.list() }
                .isInstanceOf(TemplateException.TemplateInvalidException::class.java)
        } finally {
            jdbcTemplate.update("delete from template where id = ?", invalidTemplateId)
        }
    }

    @Test
    fun `템플릿 조회 서비스 상세 조회는 표시 순서대로 선수 목록을 반환한다`() {
        val template =
            templateRepository.save(
                Template.createDraft(
                    name = "순서 검증",
                    teamCount = 2,
                    teamSize = 3,
                    strategy = DraftOrderStrategy.SNAKE,
                    playerNames = listOf("선수3", "선수1", "선수2", "선수4"),
                ),
            )

        val detail = templateFinder.getDetail(TemplateId(template.templateId))

        assertThat(detail.players.map { it.name }).containsExactly("선수1", "선수2", "선수3", "선수4")
    }

    @Test
    fun `템플릿 목록 계약은 애그리거트 기반 상세 조회를 설계 정보로 변환한다`() {
        val template =
            templateRepository.save(
                Template.createDraft(
                    name = "snapshot 검증",
                    teamCount = 2,
                    teamSize = 3,
                    strategy = DraftOrderStrategy.FIXED,
                    playerNames = listOf("선수4", "선수1", "선수3", "선수2"),
                ),
            )

        val blueprint = templateCatalog.getTemplateBlueprint(template.templateId)

        assertThat(blueprint.mode).isEqualTo(TemplateMode.DRAFT)
        assertThat(blueprint.teamCount).isEqualTo(2)
        assertThat(blueprint.teamSize).isEqualTo(3)
        assertThat(blueprint.draftOrderStrategy).isEqualTo(TemplateDraftOrderStrategy.FIXED)
        assertThat(blueprint.players.map { it.name }).containsExactly("선수1", "선수2", "선수3", "선수4")
    }

    @Test
    fun `템플릿 목록 계약은 존재하지 않는 템플릿을 찾을 수 없음 예외로 변환한다`() {
        assertThatThrownBy { templateCatalog.getTemplateBlueprint(999_999L) }
            .isInstanceOf(TemplateCatalogException.NotFound::class.java)
    }

    @Test
    fun `템플릿 목록 계약은 유효하지 않은 선수 구성을 유효성 예외로 변환한다`() {
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

        jdbcTemplate.update(
            """
            insert into template_player (template_id, name, display_order)
            values (?, ?, ?)
            """.trimIndent(),
            templateId,
            "선수1",
            0,
        )

        assertThatThrownBy { templateCatalog.getTemplateBlueprint(templateId) }
            .isInstanceOf(TemplateCatalogException.Invalid::class.java)
    }
}
