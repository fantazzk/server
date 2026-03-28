package com.naminhyeok.fantazzk.bootstrap.teambuilding

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.TestConstructor

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class TeamBuildingSmokeTest(
    private val restTemplate: TestRestTemplate,
) {
    @Test
    fun `헬스체크가 정상 응답한다`() {
        restTemplate.getForEntity("/actuator/health/liveness", Any::class.java).also {
            assertThat(it.statusCode).isEqualTo(HttpStatus.OK)
        }

        restTemplate.getForEntity("/actuator/health/readiness", Any::class.java).also {
            assertThat(it.statusCode).isEqualTo(HttpStatus.OK)
        }
    }

    @Test
    fun `템플릿 생성 후 방을 만들고 조회할 수 있다`() {
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }

        val templateResponse =
            restTemplate.postForEntity(
                "/api/v1/templates",
                HttpEntity(
                    """
                    {
                        "name": "테스트 경매",
                        "mode": "AUCTION",
                        "teamCount": 2,
                        "teamSize": 2,
                        "budget": 300,
                        "playerNames": ["선수1", "선수2"]
                    }
                    """.trimIndent(),
                    headers,
                ),
                String::class.java,
            )
        assertThat(templateResponse.statusCode).isEqualTo(HttpStatus.CREATED)

        val templateId = Regex(""""id":(\d+)""").find(templateResponse.body!!)!!.groupValues[1]

        val roomResponse =
            restTemplate.postForEntity(
                "/api/v1/rooms",
                HttpEntity("""{"templateId": $templateId, "hostNickname": "호스트"}""", headers),
                String::class.java,
            )
        assertThat(roomResponse.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(roomResponse.body).contains("\"status\":\"WAITING\"")

        val code = Regex(""""code":"(\w+)"""").find(roomResponse.body!!)!!.groupValues[1]

        val getResponse = restTemplate.getForEntity("/api/v1/rooms/$code", String::class.java)
        assertThat(getResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(getResponse.body).contains("호스트")
    }
}
