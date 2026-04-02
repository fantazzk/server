package com.naminhyeok.fantazzk.template

import com.naminhyeok.fantazzk.template.application.CreateTemplateCommand
import com.naminhyeok.fantazzk.template.application.TemplateCreateService
import com.naminhyeok.fantazzk.template.application.TemplateFinder
import com.naminhyeok.fantazzk.template.spi.TemplateLookup
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
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
}
