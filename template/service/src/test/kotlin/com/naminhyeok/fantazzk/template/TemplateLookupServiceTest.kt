package com.naminhyeok.fantazzk.template

import com.naminhyeok.fantazzk.template.exception.TemplateException
import com.naminhyeok.fantazzk.template.support.InMemoryTemplatePlayerRepository
import com.naminhyeok.fantazzk.template.support.InMemoryTemplateRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TemplateLookupServiceTest {
    private lateinit var templateRepo: InMemoryTemplateRepository
    private lateinit var playerRepo: InMemoryTemplatePlayerRepository
    private lateinit var cut: TemplateLookupService

    @BeforeEach
    fun setUp() {
        templateRepo = InMemoryTemplateRepository()
        playerRepo = InMemoryTemplatePlayerRepository()
        cut = TemplateLookupServiceImpl(templateRepo, playerRepo)
    }

    @Test
    fun `ID로 템플릿을 조회할 수 있다`() {
        val saved =
            templateRepo.save(
                Template.create(
                    name = "테스트",
                    configuration = TemplateConfiguration.Auction(teamCount = 2, teamSize = 2, budgetValue = 300),
                ),
            )

        val found = cut.get(TemplateIdentity.of(saved.templateId))
        assertThat(found.name).isEqualTo("테스트")
    }

    @Test
    fun `존재하지 않는 ID로 조회하면 예외가 발생한다`() {
        assertThatThrownBy { cut.get(TemplateIdentity.of(999L)) }
            .isInstanceOf(TemplateException.TemplateNotFoundException::class.java)
    }

    @Test
    fun `존재하지 않는 ID를 find로 조회하면 null을 반환한다`() {
        val found = cut.find(TemplateIdentity.of(999L))

        assertThat(found).isNull()
    }

    @Test
    fun `find는 존재하는 템플릿을 그대로 반환한다`() {
        val saved =
            templateRepo.save(
                Template.create(
                    name = "find",
                    configuration = TemplateConfiguration.Draft(teamCount = 2, teamSize = 2, strategy = DraftOrderStrategy.SNAKE),
                ),
            )

        val found = cut.find(TemplateIdentity.of(saved.templateId))

        assertThat(found).isNotNull
        assertThat(found!!.templateId).isEqualTo(saved.templateId)
        assertThat(found.name).isEqualTo("find")
    }

    @Test
    fun `전체 템플릿 목록을 조회할 수 있다`() {
        templateRepo.save(
            Template.create(
                name = "첫째",
                configuration = TemplateConfiguration.Auction(teamCount = 2, teamSize = 2, budgetValue = 300),
            ),
        )
        templateRepo.save(
            Template.create(
                name = "둘째",
                configuration = TemplateConfiguration.Draft(teamCount = 2, teamSize = 2, strategy = DraftOrderStrategy.SNAKE),
            ),
        )

        val all = cut.getAll()
        assertThat(all).hasSize(2)
    }

    @Test
    fun `템플릿의 선수 목록을 조회할 수 있다`() {
        val template =
            templateRepo.save(
                Template.create(
                    name = "테스트",
                    configuration = TemplateConfiguration.Auction(teamCount = 2, teamSize = 2, budgetValue = 300),
                ),
            )
        playerRepo.saveAll(
            listOf(
                TemplatePlayer(templateId = template.templateId, name = "선수1", displayOrder = 0),
                TemplatePlayer(templateId = template.templateId, name = "선수2", displayOrder = 1),
            ),
        )

        val players = cut.getPlayers(template.templateId)
        assertThat(players).hasSize(2)
    }

    @Test
    fun `상세 조회는 템플릿과 선수 목록을 함께 반환한다`() {
        val template =
            templateRepo.save(
                Template.create(
                    name = "테스트",
                    configuration = TemplateConfiguration.Auction(teamCount = 2, teamSize = 2, budgetValue = 300),
                ),
            )
        playerRepo.saveAll(
            listOf(
                TemplatePlayer(templateId = template.templateId, name = "선수1", displayOrder = 0),
                TemplatePlayer(templateId = template.templateId, name = "선수2", displayOrder = 1),
            ),
        )

        val detail = cut.getDetail(TemplateIdentity.of(template.templateId))

        assertThat(detail.template.templateId).isEqualTo(template.templateId)
        assertThat(detail.players.map { it.name }).containsExactly("선수1", "선수2")
    }

    @Test
    fun `상세 조회는 선수 수가 exact count를 만족하지 않으면 템플릿 invalid 예외를 던진다`() {
        val template =
            templateRepo.save(
                Template.create(
                    name = "테스트",
                    configuration = TemplateConfiguration.Auction(teamCount = 2, teamSize = 2, budgetValue = 300),
                ),
            )
        playerRepo.saveAll(
            listOf(
                TemplatePlayer(templateId = template.templateId, name = "선수1", displayOrder = 0),
            ),
        )

        assertThatThrownBy { cut.getDetail(TemplateIdentity.of(template.templateId)) }
            .isInstanceOf(TemplateException.TemplateInvalidException::class.java)
    }
}
