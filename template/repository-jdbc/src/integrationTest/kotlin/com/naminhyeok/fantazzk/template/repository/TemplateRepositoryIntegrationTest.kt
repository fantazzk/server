package com.naminhyeok.fantazzk.template.repository

import com.naminhyeok.fantazzk.template.TeamBuildingMode
import com.naminhyeok.fantazzk.template.Template
import com.naminhyeok.fantazzk.template.TemplateIdentity
import com.naminhyeok.fantazzk.template.TemplatePlayer
import com.naminhyeok.fantazzk.template.config.TemplateJdbcConfiguration
import com.naminhyeok.fantazzk.template.of
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration
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
) {
    @Test
    fun `템플릿을 저장하고 조회할 수 있다`() {
        val saved =
            cut.save(
                Template(
                    name = "테스트 경매",
                    mode = TeamBuildingMode.AUCTION,
                    teamCount = 5,
                    teamSize = 5,
                    budget = 300,
                ),
            )

        assertThat(saved.templateId).isGreaterThan(0)

        val found = cut.findById(TemplateIdentity.of(saved.templateId))
        assertThat(found).isNotNull
        assertThat(found!!.mode).isEqualTo(TeamBuildingMode.AUCTION)
        assertThat(found.budget).isEqualTo(300)
    }

    @Test
    fun `전체 템플릿 목록을 조회할 수 있다`() {
        cut.save(Template(name = "첫째", mode = TeamBuildingMode.AUCTION, teamCount = 2, teamSize = 2, budget = 300))
        cut.save(Template(name = "둘째", mode = TeamBuildingMode.DRAFT, teamCount = 2, teamSize = 2))

        val all = cut.findAll()
        assertThat(all).hasSizeGreaterThanOrEqualTo(2)
        assertThat(all.map { it.name }).contains("첫째", "둘째")
    }

    @Test
    fun `템플릿 선수를 저장하고 조회할 수 있다`() {
        val template =
            cut.save(
                Template(name = "드래프트", mode = TeamBuildingMode.DRAFT, teamCount = 2, teamSize = 3),
            )

        templatePlayerRepository.saveAll(
            listOf(
                TemplatePlayer(templateId = template.templateId, name = "선수1", displayOrder = 0),
                TemplatePlayer(templateId = template.templateId, name = "선수2", displayOrder = 1),
            ),
        )

        val found = templatePlayerRepository.findByTemplateId(template.templateId)
        assertThat(found).hasSize(2)
        assertThat(found.map { it.name }).containsExactly("선수1", "선수2")
    }
}
