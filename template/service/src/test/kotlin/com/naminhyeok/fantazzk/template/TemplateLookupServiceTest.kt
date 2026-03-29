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
                Template(name = "테스트", mode = TeamBuildingMode.AUCTION, teamCount = 2, teamSize = 2, budget = 300),
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
    fun `전체 템플릿 목록을 조회할 수 있다`() {
        templateRepo.save(Template(name = "첫째", mode = TeamBuildingMode.AUCTION, teamCount = 2, teamSize = 2, budget = 300))
        templateRepo.save(Template(name = "둘째", mode = TeamBuildingMode.DRAFT, teamCount = 2, teamSize = 2))

        val all = cut.getAll()
        assertThat(all).hasSize(2)
    }

    @Test
    fun `템플릿의 선수 목록을 조회할 수 있다`() {
        val template =
            templateRepo.save(
                Template(name = "테스트", mode = TeamBuildingMode.AUCTION, teamCount = 2, teamSize = 2, budget = 300),
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
}
