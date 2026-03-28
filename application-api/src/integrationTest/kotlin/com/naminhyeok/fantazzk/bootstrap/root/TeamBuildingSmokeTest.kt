package com.naminhyeok.fantazzk.bootstrap.root

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.http.server.LocalTestWebServer
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TeamBuildingSmokeTest {
    @Test
    fun `템플릿 생성 후 방을 만들고 조회할 수 있다`(localTestWebServer: LocalTestWebServer) {
        val client = RestClient.create(localTestWebServer.uri())

        val templateBody =
            """
            {
                "name": "테스트 경매",
                "mode": "AUCTION",
                "teamCount": 2,
                "teamSize": 2,
                "budget": 300,
                "players": [{"name": "선수1"}, {"name": "선수2"}]
            }
            """.trimIndent()

        val templateResponse =
            client
                .post()
                .uri("/api/v1/templates")
                .contentType(MediaType.APPLICATION_JSON)
                .body(templateBody)
                .retrieve()
                .body(String::class.java)

        assertThat(templateResponse).isNotNull
        val templateId = Regex(""""id":(\d+)""").find(templateResponse!!)!!.groupValues[1]

        val roomBody = """{"templateId": $templateId, "hostNickname": "호스트"}"""
        val roomResponse =
            client
                .post()
                .uri("/api/v1/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .body(roomBody)
                .retrieve()
                .body(String::class.java)

        assertThat(roomResponse).isNotNull
        val code = Regex(""""code":"(\w+)"""").find(roomResponse!!)!!.groupValues[1]
        assertThat(code).hasSize(6)

        val getResponse =
            client
                .get()
                .uri("/api/v1/rooms/$code")
                .retrieve()
                .body(String::class.java)

        assertThat(getResponse).contains("\"status\":\"WAITING\"")
    }
}
