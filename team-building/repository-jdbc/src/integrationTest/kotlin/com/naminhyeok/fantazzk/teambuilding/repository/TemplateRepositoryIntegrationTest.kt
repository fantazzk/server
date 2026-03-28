package com.naminhyeok.fantazzk.teambuilding.repository

import com.naminhyeok.fantazzk.teambuilding.TeamBuildingMode
import com.naminhyeok.fantazzk.teambuilding.template.Template
import com.naminhyeok.fantazzk.teambuilding.template.TemplateIdentity
import com.naminhyeok.fantazzk.teambuilding.template.of
import com.naminhyeok.fantazzk.teambuilding.template.repository.TemplateRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class TemplateRepositoryIntegrationTest {
    @Autowired
    lateinit var templateRepository: TemplateRepository

    @Test
    fun `템플릿을 저장하고 조회할 수 있다`() {
        val saved =
            templateRepository.save(
                Template(
                    name = "테스트 경매",
                    mode = TeamBuildingMode.AUCTION,
                    teamCount = 5,
                    teamSize = 5,
                    budget = 300,
                ),
            )

        assertThat(saved.templateId).isGreaterThan(0)
        assertThat(saved.name).isEqualTo("테스트 경매")

        val found = templateRepository.findById(TemplateIdentity.of(saved.templateId))
        assertThat(found).isNotNull
        assertThat(found!!.mode).isEqualTo(TeamBuildingMode.AUCTION)
        assertThat(found.budget).isEqualTo(300)
    }
}
