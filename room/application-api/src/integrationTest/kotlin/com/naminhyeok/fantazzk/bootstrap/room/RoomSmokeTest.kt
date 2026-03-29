package com.naminhyeok.fantazzk.bootstrap.room

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
class RoomSmokeTest(
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
}
