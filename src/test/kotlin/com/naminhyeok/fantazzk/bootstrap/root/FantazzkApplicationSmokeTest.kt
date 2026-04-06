package com.naminhyeok.fantazzk.bootstrap.root

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@ActiveProfiles("production", "test")
class FantazzkApplicationSmokeTest(
    private val restTemplate: TestRestTemplate,
) {
    @Test
    fun `프로덕션에서 헬스 probe는 열려 있고 loggers는 노출되지 않는다`() {
        restTemplate.getForEntity("/actuator/health/liveness", Any::class.java).also {
            assertThat(it.statusCode).isEqualTo(HttpStatus.OK)
        }

        restTemplate.getForEntity("/actuator/health/readiness", Any::class.java).also {
            assertThat(it.statusCode).isEqualTo(HttpStatus.OK)
        }

        restTemplate.getForEntity("/actuator/loggers", Any::class.java).also {
            assertThat(it.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        }
    }
}
