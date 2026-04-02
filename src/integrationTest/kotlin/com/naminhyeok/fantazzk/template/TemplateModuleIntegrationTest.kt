package com.naminhyeok.fantazzk.template

import com.naminhyeok.fantazzk.template.application.CreateTemplateCommand
import com.naminhyeok.fantazzk.template.application.TemplateCreateService
import com.naminhyeok.fantazzk.template.query.TemplateQueryService
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
    lateinit var templateQueryService: TemplateQueryService

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
    fun `template create eventually updates query projection`(scenario: Scenario) {
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
            .andWaitForStateChange({ templateQueryService.listTemplates() }) { views ->
                views.any { it.name == "프로젝션 템플릿" }
            }
            .andVerify { views ->
                assertThat(views.map { it.name }).contains("프로젝션 템플릿")
            }
    }
}
