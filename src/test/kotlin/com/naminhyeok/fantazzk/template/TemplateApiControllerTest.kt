package com.naminhyeok.fantazzk.template

import com.naminhyeok.fantazzk.template.application.CreateTemplate
import com.naminhyeok.fantazzk.template.application.CreateTemplateCommand
import com.naminhyeok.fantazzk.template.application.FindTemplates
import com.naminhyeok.fantazzk.template.application.TemplateDetail
import com.naminhyeok.fantazzk.template.domain.Template
import com.naminhyeok.fantazzk.template.domain.TemplatePlayer
import com.naminhyeok.fantazzk.template.exception.TemplateException
import com.naminhyeok.fantazzk.template.web.TemplateApiController
import com.naminhyeok.fantazzk.template.web.TemplateExceptionHandler
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class TemplateApiControllerTest {
    private val templateCreateService: CreateTemplate = mockk()
    private val templateFinder: FindTemplates = mockk()

    private val mockMvc: MockMvc =
        MockMvcBuilders
            .standaloneSetup(TemplateApiController(templateCreateService, templateFinder))
            .setControllerAdvice(TemplateExceptionHandler())
            .build()

    @Nested
    inner class `템플릿 생성` {
        @Test
        fun `유효한 요청으로 템플릿을 생성하면 201을 반환한다`() {
            val template = template()
            every {
                templateCreateService.create(
                    CreateTemplateCommand.Auction(
                        name = "경매전",
                        teamCount = 2,
                        teamSize = 2,
                        budget = 300,
                        playerNames = listOf("선수1", "선수2"),
                    ),
                )
            } returns template

            mockMvc.post("/api/v1/templates") {
                contentType = MediaType.APPLICATION_JSON
                content =
                    """
                    {
                        "name": "경매전",
                        "mode": "AUCTION",
                        "teamCount": 2,
                        "teamSize": 2,
                        "budget": 300,
                        "playerNames": ["선수1", "선수2"]
                    }
                    """.trimIndent()
            }.andExpect {
                status { isCreated() }
                jsonPath("$.resultType") { value("SUCCESS") }
                jsonPath("$.success.name") { value("경매전") }
                jsonPath("$.success.mode") { value("AUCTION") }
                jsonPath("$.success.budget") { value(300) }
            }
        }

        @Test
        fun `선수 수가 exact count와 다르면 400을 반환한다`() {
            every {
                templateCreateService.create(
                    CreateTemplateCommand.Auction(
                        name = "실패",
                        teamCount = 2,
                        teamSize = 2,
                        budget = 300,
                        playerNames = listOf("선수1"),
                    ),
                )
            } throws IllegalArgumentException("선수 수는 정확히 2명이어야 합니다")

            mockMvc.post("/api/v1/templates") {
                contentType = MediaType.APPLICATION_JSON
                content =
                    """
                    {
                        "name": "실패",
                        "mode": "AUCTION",
                        "teamCount": 2,
                        "teamSize": 2,
                        "budget": 300,
                        "playerNames": ["선수1"]
                    }
                    """.trimIndent()
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.resultType") { value("ERROR") }
                jsonPath("$.error.errorCode") { value("BAD_REQUEST") }
                jsonPath("$.error.reason") { value("선수 수는 정확히 2명이어야 합니다") }
            }
        }

        @Test
        fun `드래프트 요청이 예산을 포함하면 400을 반환한다`() {
            mockMvc.post("/api/v1/templates") {
                contentType = MediaType.APPLICATION_JSON
                content =
                    """
                    {
                        "name": "드래프트",
                        "mode": "DRAFT",
                        "teamCount": 2,
                        "teamSize": 2,
                        "budget": 300,
                        "draftOrderStrategy": "SNAKE",
                        "playerNames": ["선수1", "선수2"]
                    }
                    """.trimIndent()
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.resultType") { value("ERROR") }
                jsonPath("$.error.errorCode") { value("BAD_REQUEST") }
                jsonPath("$.error.reason") { value("드래프트 템플릿에는 예산을 지정할 수 없습니다") }
            }
        }
    }

    @Nested
    inner class `템플릿 조회` {
        @Test
        fun `ID로 템플릿을 조회하면 200과 선수 목록을 반환한다`() {
            val template = template()
            val players =
                listOf(
                    TemplatePlayer(templateId(1), "선수1", 0),
                    TemplatePlayer(templateId(1), "선수2", 1),
                )
            every { templateFinder.getDetail(templateId(1)) } returns TemplateDetail(template, players)

            mockMvc.get("/api/v1/templates/${templateIdText(1)}")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.resultType") { value("SUCCESS") }
                    jsonPath("$.success.name") { value("경매전") }
                    jsonPath("$.success.players[0].name") { value("선수1") }
                }

            verify(exactly = 1) { templateFinder.getDetail(templateId(1)) }
        }

        @Test
        fun `존재하지 않는 ID로 조회하면 404를 반환한다`() {
            every { templateFinder.getDetail(templateId(999)) } throws TemplateException.TemplateNotFoundException()

            mockMvc.get("/api/v1/templates/${templateIdText(999)}")
                .andExpect {
                    status { isNotFound() }
                    jsonPath("$.resultType") { value("ERROR") }
                    jsonPath("$.error.status") { value(404) }
                    jsonPath("$.error.errorCode") { value("TEMPLATE_NOT_FOUND") }
                    jsonPath("$.error.reason") { value("템플릿을 찾을 수 없습니다") }
                }

            verify(exactly = 1) { templateFinder.getDetail(templateId(999)) }
        }

        @Test
        fun `전체 목록을 조회하면 200을 반환한다`() {
            every { templateFinder.list() } returns listOf(template())

            mockMvc.get("/api/v1/templates")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.resultType") { value("SUCCESS") }
                    jsonPath("$.success[0].name") { value("경매전") }
                }
        }
    }

    private fun template() =
        Template.createAuction("경매전", 2, 2, 300, listOf("선수1", "선수2"))
            .assignId(templateId(1))

    private fun templateId(number: Long): TemplateId = TemplateId.from(templateIdText(number))

    private fun templateIdText(number: Long): String = "00000000-0000-0000-0000-${number.toString().padStart(12, '0')}"
}
