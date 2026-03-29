package com.naminhyeok.fantazzk.bootstrap.root

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestConstructor

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class FantazzkOpenApiDocumentationIntegrationTest(
    private val restTemplate: TestRestTemplate,
) {
    private val objectMapper = ObjectMapper()

    @Test
    fun `방 상태 전이 API 문서에 설명과 대표 에러 응답이 노출된다`() {
        val document = openApiDocument()
        val startOperation = document.at("/paths/~1api~1v1~1rooms~1{code}~1start/post")

        assertThat(startOperation.at("/tags/0").asText()).isEqualTo("Room")
        assertThat(startOperation.at("/description").asText())
            .contains("WAITING")
            .contains("IN_PROGRESS")
        assertThat(startOperation.at("/responses/409/description").asText()).isEqualTo("현재 상태에서는 방을 시작할 수 없습니다")
        assertThat(
            startOperation.at("/responses/409/content/application~1json/examples/invalidState/value/error/errorCode").asText(),
        ).isEqualTo("INVALID_STATE")
    }

    @Test
    fun `생성 API 문서에 요청 예시와 성공 응답 예시가 노출된다`() {
        val document = openApiDocument()
        val roomCreateOperation = document.at("/paths/~1api~1v1~1rooms/post")
        val templateCreateOperation = document.at("/paths/~1api~1v1~1templates/post")

        assertThat(
            roomCreateOperation.at("/requestBody/content/application~1json/examples/createRoom/value/hostNickname").asText(),
        ).isEqualTo("호스트")
        assertThat(
            roomCreateOperation.at("/responses/201/content/application~1json/examples/createdRoom/value/success/code").asText(),
        ).isEqualTo("ROOM01")
        assertThat(
            roomCreateOperation.at("/responses/500/content/application~1json/examples/internalError/value/error/errorCode").asText(),
        ).isEqualTo("INTERNAL_ERROR")

        assertThat(
            templateCreateOperation.at("/requestBody/content/application~1json/examples/createAuctionTemplate/value/mode").asText(),
        ).isEqualTo("AUCTION")
        assertThat(
            templateCreateOperation.at("/responses/201/content/application~1json/examples/createdTemplate/value/success/name").asText(),
        ).isEqualTo("주말 풋살 경매전")
        assertThat(
            templateCreateOperation.at("/responses/400/content/application~1json/examples/teamCountMustBePositive/value/error/reason").asText(),
        ).isEqualTo("팀 수는 0보다 커야 합니다")
    }

    @Test
    fun `핵심 DTO schema 설명이 OpenAPI components에 노출된다`() {
        val document = openApiDocument()
        val schemas = document.at("/components/schemas").toString()

        assertThat(schemas)
            .contains("방 생성 요청입니다.")
            .contains("호스트")
            .contains("방 참가 요청입니다.")
            .contains("경매 입찰 요청입니다.")
            .contains("드래프트 픽 요청입니다.")
            .contains("템플릿 생성 요청입니다.")
            .contains("DRAFT 모드")
            .contains("playerNames")
    }

    @Test
    fun `OpenAPI 소개 문구에 공통 응답 envelope 설명이 포함된다`() {
        val document = openApiDocument()

        assertThat(document.at("/info/description").asText())
            .contains("resultType")
            .contains("success")
            .contains("error")
    }

    private fun openApiDocument(): JsonNode {
        val response = restTemplate.getForEntity("/v3/api-docs", String::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        return response.body?.let(objectMapper::readTree)
            ?: error("OpenAPI document body is empty")
    }
}
