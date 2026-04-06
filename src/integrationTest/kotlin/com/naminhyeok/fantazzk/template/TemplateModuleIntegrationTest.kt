package com.naminhyeok.fantazzk.template

import com.naminhyeok.fantazzk.template.application.CreateTemplate
import com.naminhyeok.fantazzk.template.application.CreateTemplateCommand
import com.naminhyeok.fantazzk.template.application.FindTemplates
import com.naminhyeok.fantazzk.template.domain.DraftOrderStrategy
import com.naminhyeok.fantazzk.template.domain.Template
import com.naminhyeok.fantazzk.template.exception.TemplateException
import com.naminhyeok.fantazzk.template.repository.Templates
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.modulith.test.ApplicationModuleTest
import java.util.UUID

@ApplicationModuleTest(
    module = "template",
    verifyAutomatically = false,
)
class TemplateModuleIntegrationTest {
    @Autowired
    lateinit var templateCreateService: CreateTemplate

    @Autowired
    lateinit var templateCatalog: TemplateCatalog

    @Autowired
    lateinit var templateFinder: FindTemplates

    @Autowired
    lateinit var templateRepository: Templates

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun `템플릿 모듈은 루트 계약과 함께 단독 부팅된다`() {
        assertThat(templateCreateService).isNotNull()
        assertThat(templateCatalog).isNotNull()
    }

    @Test
    fun `템플릿 생성은 저장 직후 상세 조회가 가능하다`() {
        val created =
            templateCreateService.create(
                CreateTemplateCommand.Auction(
                    "모듈 테스트 템플릿",
                    2,
                    2,
                    300,
                    listOf("선수1", "선수2"),
                ),
            )

        val detail = templateFinder.getDetail(created.id)

        assertThat(detail.template.name).isEqualTo("모듈 테스트 템플릿")
        assertThat(detail.players.map { it.name }).containsExactly("선수1", "선수2")
    }

    @Test
    fun `템플릿 생성 이후 목록 조회에서 새 템플릿을 바로 조회할 수 있다`() {
        templateCreateService.create(
            CreateTemplateCommand.Auction(
                "프로젝션 템플릿",
                2,
                2,
                300,
                listOf("선수1", "선수2"),
            ),
        )

        val templates = templateFinder.list()
        assertThat(templates.map { it.name }).contains("프로젝션 템플릿")
    }

    @Test
    fun `template finder 는 별도 선수 리포지토리 없이 aggregate 에서 선수 목록을 읽는다`() {
        val created =
            templateCreateService.create(
                CreateTemplateCommand.Auction(
                    "집약 루트",
                    2,
                    2,
                    300,
                    listOf("선수1", "선수2"),
                ),
            )

        val detail = templateFinder.getDetail(created.id)

        assertThat(detail.players.map { it.name }).containsExactly("선수1", "선수2")
    }

    @Test
    fun `템플릿 조회 서비스 목록 조회는 유효하지 않은 행을 템플릿 유효성 예외로 변환한다`() {
        templateRepository.save(
            Template.createAuction(
                "정상 템플릿",
                2,
                2,
                300,
                listOf("선수1", "선수2"),
            ),
        )
        val invalidTemplateId = UUID.fromString("00000000-0000-0000-0000-00000000e001")
        jdbcTemplate.update(
            """
            insert into template (id, name, mode, team_count, team_size, budget, draft_order_strategy)
            values (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            invalidTemplateId,
            "유효하지 않은 템플릿",
            "AUCTION",
            2,
            2,
            null,
            null,
        )

        try {
            assertThatThrownBy { templateFinder.list() }
                .isInstanceOf(TemplateException.TemplateInvalidException::class.java)
        } finally {
            jdbcTemplate.update("delete from template where id = ?", invalidTemplateId)
        }
    }

    @Test
    fun `템플릿 조회 서비스 목록 조회는 선수 구성이 불완전한 행을 템플릿 유효성 예외로 변환한다`() {
        val templateId = UUID.fromString("00000000-0000-0000-0000-00000000e002")
        jdbcTemplate.update(
            """
            insert into template (id, name, mode, team_count, team_size, budget, draft_order_strategy)
            values (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            templateId,
            "선수 구성 누락 템플릿",
            "AUCTION",
            2,
            2,
            300,
            null,
        )

        jdbcTemplate.update(
            """
            insert into template_player (id, template_id, name, display_order)
            values (?, ?, ?, ?)
            """.trimIndent(),
            UUID.fromString("00000000-0000-0000-0000-00000000e102"),
            templateId,
            "선수1",
            0,
        )

        try {
            assertThatThrownBy { templateFinder.list() }
                .isInstanceOf(TemplateException.TemplateInvalidException::class.java)
        } finally {
            jdbcTemplate.update("delete from template_player where template_id = ?", templateId)
            jdbcTemplate.update("delete from template where id = ?", templateId)
        }
    }

    @Test
    fun `템플릿 조회 서비스 상세 조회는 표시 순서대로 선수 목록을 반환한다`() {
        val template =
            templateRepository.save(
                Template.createDraft(
                    "순서 검증",
                    2,
                    3,
                    DraftOrderStrategy.SNAKE,
                    listOf("선수3", "선수1", "선수2", "선수4"),
                ),
            )

        val detail = templateFinder.getDetail(template.id)

        assertThat(detail.players.map { it.name }).containsExactly("선수3", "선수1", "선수2", "선수4")
    }

    @Test
    fun `템플릿 목록 계약은 애그리거트 기반 상세 조회를 설계 정보로 변환한다`() {
        val template =
            templateRepository.save(
                Template.createDraft(
                    "snapshot 검증",
                    2,
                    3,
                    DraftOrderStrategy.FIXED,
                    listOf("선수4", "선수1", "선수3", "선수2"),
                ),
            )

        val blueprint = templateCatalog.getTemplateBlueprint(template.id)

        assertThat(blueprint.mode).isEqualTo(TemplateMode.DRAFT)
        assertThat(blueprint.teamCount).isEqualTo(2)
        assertThat(blueprint.teamSize).isEqualTo(3)
        assertThat(blueprint.draftOrderStrategy).isEqualTo(TemplateDraftOrderStrategy.FIXED)
        assertThat(blueprint.players.map { it.name }).containsExactly("선수4", "선수1", "선수3", "선수2")
    }

    @Test
    fun `템플릿 목록 계약은 존재하지 않는 템플릿을 찾을 수 없음 예외로 변환한다`() {
        assertThatThrownBy { templateCatalog.getTemplateBlueprint(templateId(999_999)) }
            .isInstanceOf(TemplateCatalogException.NotFound::class.java)
    }

    @Test
    fun `템플릿 목록 계약은 유효하지 않은 선수 구성을 유효성 예외로 변환한다`() {
        val templateId = UUID.fromString("00000000-0000-0000-0000-00000000e003")
        jdbcTemplate.update(
            """
            insert into template (id, name, mode, team_count, team_size, budget, draft_order_strategy)
            values (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            templateId,
            "유효하지 않은 템플릿",
            "AUCTION",
            2,
            2,
            300,
            null,
        )

        jdbcTemplate.update(
            """
            insert into template_player (id, template_id, name, display_order)
            values (?, ?, ?, ?)
            """.trimIndent(),
            UUID.fromString("00000000-0000-0000-0000-00000000e103"),
            templateId,
            "선수1",
            0,
        )

        try {
            assertThatThrownBy { templateCatalog.getTemplateBlueprint(TemplateId.of(templateId.toString())) }
                .isInstanceOf(TemplateCatalogException.Invalid::class.java)
        } finally {
            jdbcTemplate.update("delete from template_player where template_id = ?", templateId)
            jdbcTemplate.update("delete from template where id = ?", templateId)
        }
    }

    private fun templateId(value: Long): TemplateId =
        TemplateId.of("00000000-0000-0000-0000-${value.toString(16).padStart(12, '0')}")
}
