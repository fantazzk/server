package com.naminhyeok.fantazzk.template

import com.naminhyeok.fantazzk.template.exception.TemplateException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.Instant

class TemplateApiControllerTest {
    private val templateCreateService: TemplateCreateService = mockk()
    private val templateLookupService: TemplateLookupService = mockk()

    private val now = Instant.now()

    private val mockMvc: MockMvc =
        MockMvcBuilders
            .standaloneSetup(TemplateApiController(templateCreateService, templateLookupService))
            .setControllerAdvice(TemplateExceptionHandler())
            .build()

    @Nested
    inner class `템플릿 생성` {
        @Test
        fun `유효한 요청으로 템플릿을 생성하면 201을 반환한다`() {
            val template = template()
            every {
                templateCreateService.create("경매전", TeamBuildingMode.AUCTION, 2, 3, 300, null, listOf("선수1", "선수2"))
            } returns template

            mockMvc.post("/api/v1/templates") {
                contentType = MediaType.APPLICATION_JSON
                content =
                    """
                    {
                        "name": "경매전",
                        "mode": "AUCTION",
                        "teamCount": 2,
                        "teamSize": 3,
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
        fun `유효하지 않은 값으로 생성하면 400을 반환한다`() {
            every {
                templateCreateService.create(any(), any(), eq(0), any(), any(), any(), any())
            } throws IllegalArgumentException("팀 수는 1 이상이어야 합니다")

            mockMvc.post("/api/v1/templates") {
                contentType = MediaType.APPLICATION_JSON
                content =
                    """
                    {
                        "name": "실패",
                        "mode": "AUCTION",
                        "teamCount": 0,
                        "teamSize": 2,
                        "budget": 300,
                        "playerNames": []
                    }
                    """.trimIndent()
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.resultType") { value("ERROR") }
                jsonPath("$.error.errorCode") { value("BAD_REQUEST") }
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
                    TemplatePlayer(
                        templatePlayerId = 1L,
                        templateId = 1L,
                        name = "선수1",
                        displayOrder = 0,
                        createdAt = now,
                        updatedAt = now,
                    ),
                )
            every { templateLookupService.get(any()) } returns template
            every { templateLookupService.getPlayers(1L) } returns players

            mockMvc.get("/api/v1/templates/1")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.resultType") { value("SUCCESS") }
                    jsonPath("$.success.name") { value("경매전") }
                    jsonPath("$.success.players[0].name") { value("선수1") }
                }
        }

        @Test
        fun `존재하지 않는 ID로 조회하면 404를 반환한다`() {
            every { templateLookupService.get(any()) } throws TemplateException.TemplateNotFoundException()

            mockMvc.get("/api/v1/templates/999")
                .andExpect {
                    status { isNotFound() }
                    jsonPath("$.resultType") { value("ERROR") }
                    jsonPath("$.error.status") { value(404) }
                    jsonPath("$.error.errorCode") { value("TEMPLATE_NOT_FOUND") }
                    jsonPath("$.error.reason") { value("템플릿을 찾을 수 없습니다") }
                }
        }

        @Test
        fun `전체 목록을 조회하면 200을 반환한다`() {
            every { templateLookupService.getAll() } returns listOf(template())

            mockMvc.get("/api/v1/templates")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.resultType") { value("SUCCESS") }
                    jsonPath("$.success[0].name") { value("경매전") }
                }
        }
    }

    private fun template() =
        Template(
            templateId = 1L,
            name = "경매전",
            mode = TeamBuildingMode.AUCTION,
            teamCount = 2,
            teamSize = 3,
            budget = 300,
            createdAt = now,
            updatedAt = now,
        )
}
